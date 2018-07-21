package api

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/TravisS25/httputil/apiutil"
	"github.com/TravisS25/httputil/confutil"
	"github.com/TravisS25/inventory-tracking/src/server/config"
	"github.com/TravisS25/inventory-tracking/src/server/forms"
	"github.com/TravisS25/inventory-tracking/src/server/models"
)

var (
	user = models.UserProfile{
		TitleID:    2,
		Email:      "testuser@email.com",
		Password:   TestPasswordHash,
		FirstName:  "First",
		LastName:   "Last",
		IsActive:   true,
		DateJoined: time.Now().UTC().Format(confutil.DateTimeLayout),
	}
)

func TestLogin(t *testing.T) {
	err := user.Insert(TestDB)

	if err != nil {
		t.Fatal("Could not insert test user")
	}

	testCase1 := apiutil.TestCase{
		TestName:       "login1",
		Method:         "GET",
		RequestURL:     config.RouterPaths["login"],
		ExpectedStatus: http.StatusOK,
		Handler:        IntegrationTestRouter,
	}

	testCase2 := testCase1
	testCase2.TestName = "login2"
	testCase2.Method = "POST"
	testCase2.Form = forms.LoginForm{
		Email:    "testuser@email.com",
		Password: TestPassword,
	}

	testCase3 := testCase2
	testCase3.TestName = "login3"
	testCase3.ExpectedStatus = http.StatusNotAcceptable
	testCase3.ExpectedBody = apiutil.ErrBodyMessage.Error()
	testCase3.Form = nil

	testCase4 := testCase3
	testCase4.TestName = "login4"
	testCase4.Form = "Invalid json"
	testCase4.ExpectedBody = apiutil.ErrInvalidJSON.Error()

	testCase5 := testCase4
	testCase5.TestName = "login5"
	testCase5.Form = forms.LoginForm{}
	testCase5.ExpectedBody = ""

	apiutil.RunTestCases(t, []apiutil.TestCase{
		testCase1,
		testCase2,
		testCase3,
		testCase4,
		testCase5,
	})

	err = user.Delete(TestDB)

	if err != nil {
		t.Fatal("Could not delete user")
	}
}

func TestChangePassword(t *testing.T) {
	err := user.Insert(TestDB)

	if err != nil {
		t.Fatal("Could not insert test user")
	}

	testCase1 := apiutil.TestCase{
		TestName:       "changePassword1",
		Method:         "GET",
		RequestURL:     config.RouterPaths["changePassword"],
		ExpectedStatus: http.StatusOK,
		Handler:        IntegrationTestRouter,
	}

	testCase2 := testCase1
	testCase2.TestName = "changePassword2"
	testCase2.Method = "POST"
	testCase2.Form = forms.ChangePasswordForm{
		CurrentPassword: TestPassword,
		NewPassword:     "NewPassword",
		ConfirmPassword: "NewPassword",
	}

	testCase3 := testCase2
	testCase3.TestName = "changePassword3"
	testCase3.Form = forms.ChangePasswordForm{}
	testCase3.ExpectedStatus = http.StatusNotAcceptable

	apiutil.RunTestCases(t, []apiutil.TestCase{
		testCase1,
		testCase2,
		testCase3,
	})

	err = user.Delete(TestDB)

	if err != nil {
		t.Fatal("Could not delete user")
	}
}

type Token struct {
	Token string `json:"token"`
}

func TestAccountAPIs(t *testing.T) {
	var req *http.Request
	var res *http.Response
	var err error
	var buffer bytes.Buffer
	var token, csrfCookie, userCookie string

	defer func() {
		TestDB.Exec(
			`
			update 
				user_profile
			set
				password = $1
			where
				email = $2,
			`,
			TestPasswordHash,
			WorkerEmail,
		)
	}()

	ts := httptest.NewServer(App())
	defer ts.Close()
	userCookie, err = loginUser(WorkerEmail, TestPassword, ts)

	if err != nil {
		t.Fatal("Could not login user")
	}

	client := &http.Client{}
	baseURL := ts.URL

	//loginURL := baseURL + config.RouterPaths["login"]
	logoutURL := baseURL + config.RouterPaths["logout"]
	changePasswordURL := baseURL + config.RouterPaths["changePassword"]
	resetPasswordURL := baseURL + config.RouterPaths["resetPassword"]
	confirmPasswordResetURL := baseURL + config.RouterPaths["confirmPasswordReset"]
	userDetailsURL := baseURL + config.RouterPaths["userDetails"]

	// -----------------------------------------------------------------
	//
	// User Details API

	req, _ = http.NewRequest("GET", userDetailsURL, nil)
	req.Header.Set(CookieHeader, userCookie)
	res, err = client.Do(req)

	if err != nil {
		t.Fatal("err on response")
	}

	apiutil.ResponseError(t, res, http.StatusOK, err)

	// -----------------------------------------------------------------
	//
	// ChangePassword API

	req, err = http.NewRequest("GET", changePasswordURL, nil)

	if err != nil {
		t.Fatal("err on new request")
	}

	req.Header.Set(CookieHeader, userCookie)
	res, err = client.Do(req)

	if err != nil {
		t.Fatal("err on response")
	}

	token = res.Header.Get(TokenHeader)
	csrfCookie = res.Header.Get(SetCookieHeader)
	changePasswordForm := forms.ChangePasswordForm{
		CurrentPassword: TestPassword,
		NewPassword:     "NewPassword",
		ConfirmPassword: "NewPassword",
	}
	buffer = apiutil.GetJSONBuffer(changePasswordForm)
	req, err = http.NewRequest("POST", changePasswordURL, &buffer)

	if err != nil {
		t.Fatal("err on request")
	}

	req.Header.Set(TokenHeader, token)
	req.Header.Set(CookieHeader, csrfCookie+"; "+userCookie)
	res, err = client.Do(req)
	apiutil.ResponseError(t, res, http.StatusOK, err)
	buffer.Reset()

	if err != nil {
		t.Fatal("Could not execute update query")
	}

	// -----------------------------------------------------------------
	//
	// Reset Password API

	req, err = http.NewRequest("GET", resetPasswordURL, nil)

	if err != nil {
		t.Fatal("err on new request")
	}

	//req.Header.Set(CookieHeader, userCookie)
	res, err = client.Do(req)

	if err != nil {
		t.Fatal("err on response")
	}

	token = res.Header.Get(TokenHeader)
	csrfCookie = res.Header.Get(SetCookieHeader)
	resetPasswordForm := forms.EmailForm{
		Email: WorkerEmail,
	}
	buffer = apiutil.GetJSONBuffer(resetPasswordForm)
	req, err = http.NewRequest("POST", resetPasswordURL, &buffer)

	if err != nil {
		t.Fatal("err on request")
	}

	req.Header.Set(TokenHeader, token)
	req.Header.Set(CookieHeader, csrfCookie+"; "+userCookie)
	res, err = client.Do(req)
	apiutil.ResponseError(t, res, http.StatusOK, err)
	buffer.Reset()
	buffer.ReadFrom(res.Body)

	var resetToken Token
	json.Unmarshal(buffer.Bytes(), &resetToken)

	// -----------------------------------------------------------------
	//
	// Confirm Password Reset API

	url := strings.Replace(confirmPasswordResetURL, "{token}", resetToken.Token, 1)
	req, err = http.NewRequest("GET", url, nil)

	if err != nil {
		t.Fatal("err on new request")
	}

	//req.Header.Set(CookieHeader, userCookie)
	res, err = client.Do(req)

	if err != nil {
		t.Fatal("err on response")
	}

	token = res.Header.Get(TokenHeader)
	csrfCookie = res.Header.Get(SetCookieHeader)
	confirmPasswordResetForm := forms.ConfirmPasswordForm{
		NewPassword:     "New Password",
		ConfirmPassword: "New Password",
	}
	buffer = apiutil.GetJSONBuffer(confirmPasswordResetForm)
	req, err = http.NewRequest("POST", url, &buffer)

	if err != nil {
		t.Fatal("err on request")
	}

	req.Header.Set(TokenHeader, token)
	req.Header.Set(CookieHeader, csrfCookie)
	res, err = client.Do(req)
	apiutil.ResponseError(t, res, http.StatusOK, err)
	buffer.Reset()

	_, err = TestDB.Exec(
		`update user_profile set password = $1 where email = $2`,
		TestPasswordHash,
		WorkerEmail,
	)

	// -----------------------------------------------------------------
	//
	// Logout Api

	userCookie, err = loginUser(WorkerEmail, TestPassword, ts)
	req, _ = http.NewRequest("GET", logoutURL, nil)
	req.Header.Set(CookieHeader, userCookie)
	res, err = client.Do(req)
	apiutil.ResponseError(t, res, http.StatusOK, err)

}

// ------------------------ TEST FUNCTIONS ----------------------------------

// func loggedInTest(
// 	t *testing.T,
// 	mockDB httputil.DBInterface,
// 	mockCache cacheutil.CacheStore,
// 	mockStore sessions.Store,
// 	mockMessage mailutil.SendMessage,
// ) {
// 	allowedMethods := []string{"GET", "POST"}
// 	handlerURL := "/api/account/logged-in/"
// 	requestURL := "/api/account/logged-in/"
// 	accountAPI := NewAccountAPI(mockDB, mockCache, mockStore, mockMessage)

// 	loggedInCtx := map[interface{}]interface{}{
// 		UserCtxKey: &models.UserProfile{
// 			ID:    1,
// 			Email: "loggedin@email.com",
// 		},
// 	}

// 	testCases := []TestCase{
// 		{
// 			TestName:       "loggedIn GET 200 response",
// 			Method:         "GET",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     requestURL,
// 			ExpectedStatus: http.StatusOK,
// 			ContextValues:  loggedInCtx,
// 			Handler:        accountAPI.LoggedIn,
// 		},
// 		{
// 			TestName:       "loggedIn POST 404 response",
// 			Method:         "POST",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     requestURL,
// 			ExpectedStatus: http.StatusNotFound,
// 			Handler:        accountAPI.LoggedIn,
// 		},
// 	}

// 	runTestCases(t, testCases)
// }

// func loginTest(
// 	t *testing.T,
// 	mockDB httputil.DBInterface,
// 	mockCache config.CacheStore,
// 	mockStore sessions.Store,
// 	mockMessage config.SendMessage,
// ) {
// 	allowedMethods := []string{"GET", "POST"}
// 	handlerURL := "/api/account/login/"
// 	requestURL := "/api/account/login/"
// 	accountAPI := NewAccountAPI(mockDB, mockCache, mockStore, mockMessage)

// 	switch mockDB.(type) {
// 	case *models.DB:
// 		_, err := models.QueryUserProfile(
// 			mockDB,
// 			"select email, password from user_profile where email = $1;",
// 			"notactive@email.com",
// 		)

// 		if err == sql.ErrNoRows {
// 			user := &models.UserProfile{
// 				Password:   "$2a$10$5WLGngqu3JS/RTP1Ifsg1unllfu.s9qDkL8.IFV433H1tu8r7etui",
// 				FirstName:  "Not",
// 				LastName:   "Active",
// 				Email:      "notactive@email.com",
// 				FullName:   "Not Active",
// 				AreaID:     2,
// 				DateJoined: time.Now().UTC().Format(config.DateTimeLayout),
// 			}
// 			err = user.Insert(mockDB)

// 			if err != nil {
// 				t.Error(err)
// 			}
// 		} else if err != nil && err != sql.ErrNoRows {
// 			fmt.Println("server error querying user")
// 			fmt.Println(err)
// 		}

// 		_, err = models.QueryUserProfile(
// 			mockDB,
// 			"select email, password from user_profile where Email = $1;",
// 			"active@email.com")

// 		if err == sql.ErrNoRows {
// 			user := &models.UserProfile{
// 				Password:   "$2a$10$5WLGngqu3JS/RTP1Ifsg1unllfu.s9qDkL8.IFV433H1tu8r7etui",
// 				FirstName:  "Currently",
// 				LastName:   "Active",
// 				Email:      "active@email.com",
// 				FullName:   "Currently Active",
// 				AreaID:     2,
// 				DateJoined: time.Now().UTC().Format(config.DateTimeLayout),
// 				IsActive:   true,
// 			}
// 			err = user.Insert(mockDB)

// 			if err != nil {
// 				t.Error(err)
// 			}
// 		} else if err != nil && err != sql.ErrNoRows {
// 			fmt.Println("server error querying user")
// 			fmt.Println(err)
// 		}
// 	}

// 	form1 := &forms.LoginForm{
// 		Email:    "notactive@email.com",
// 		Password: "currentpassword",
// 	}
// 	form2 := &forms.LoginForm{
// 		Email:    "active@email.com",
// 		Password: "currentpassword",
// 	}

// 	switchValues := map[string]error{
// 		"isActive": errIsActive,
// 	}

// 	scannerMap := map[string]ScannerReturn{
// 		"select email, password": ScannerReturn{
// 			scanValues: []interface{}{
// 				form1.Email,
// 				config.HashPassword,
// 			},
// 		},
// 	}

// 	testCases := []TestCase{
// 		{
// 			TestName:       "loginTest GET 200 response",
// 			Method:         "GET",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     requestURL,
// 			ExpectedStatus: http.StatusOK,
// 			Form:           nil,
// 			Handler:        accountAPI.Login,
// 		},
// 		{
// 			TestName:       "loginTest POST 406 response",
// 			Method:         "POST",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     requestURL,
// 			ExpectedStatus: http.StatusNotAcceptable,
// 			Form:           &forms.LoginForm{},
// 			Handler:        accountAPI.Login,
// 		},
// 		{
// 			TestName:       "loginTest POST 409 response",
// 			Method:         "POST",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     requestURL,
// 			ScannerMap:     scannerMap,
// 			ExpectedStatus: http.StatusConflict,
// 			Form:           form1,
// 			Handler:        accountAPI.Login,
// 		},
// 		{
// 			TestName:           "loginTest POST 200 response",
// 			Method:             "POST",
// 			AllowedMethods:     allowedMethods,
// 			HandlerURL:         handlerURL,
// 			RequestURL:         requestURL,
// 			ExpectedStatus:     http.StatusOK,
// 			ScannerMap:         scannerMap,
// 			CustomSwitchValues: switchValues,
// 			Form:               form2,
// 			Handler:            accountAPI.Login,
// 		},
// 	}

// 	runTestCases(t, testCases)
// }

// func changePasswordTest(
// 	t *testing.T,
// 	mockDB httputil.DBInterface,
// 	mockCache config.CacheStore,
// 	mockStore sessions.Store,
// 	mockMessage config.SendMessage,
// ) {
// 	allowedMethods := []string{"GET", "POST"}
// 	handlerURL := "/api/account/change-password/"
// 	requestURL := "/api/account/change-password/"
// 	accountAPI := NewAccountAPI(mockDB, mockCache, mockStore, mockMessage)

// 	user := &models.UserProfile{
// 		AreaID:      2,
// 		Password:    config.HashPassword,
// 		AdminAreaID: nil,
// 		FirstName:   "Change",
// 		LastName:    "Password",
// 		FullName:    "Change Password",
// 		Email:       "changepassword@email.com",
// 		DateJoined:  time.Now().UTC().Format(config.DateTimeLayout),
// 	}

// 	switch mockDB.(type) {
// 	case *models.DB:
// 		_, err := models.QueryUserProfile(
// 			mockDB,
// 			"select * from user_profile where email = $1;",
// 			user.Email,
// 		)

// 		if err == sql.ErrNoRows {
// 			err = user.Insert(mockDB)

// 			if err != nil {
// 				t.Error(err)
// 			}
// 		} else if err != nil && err != sql.ErrNoRows {
// 			fmt.Println("server error querying user")
// 			fmt.Println(err)
// 		}
// 	}

// 	form := &forms.ChangePasswordForm{
// 		CurrentPassword: "currentpassword",
// 		NewPassword:     "currentpassword",
// 		ConfirmPassword: "currentpassword",
// 	}

// 	ctxValues := map[interface{}]interface{}{
// 		UserCtxKey: user,
// 	}

// 	testCases := []TestCase{
// 		{
// 			TestName:       "changePasswordTest 1 GET 200 response",
// 			Method:         "GET",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     requestURL,
// 			ExpectedStatus: http.StatusOK,
// 			Form:           nil,
// 			Handler:        accountAPI.ChangePassword,
// 		},
// 		{
// 			TestName:       "changePasswordTest 2 POST 406 response",
// 			Method:         "POST",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     requestURL,
// 			ExpectedStatus: http.StatusNotAcceptable,
// 			Form:           &forms.ChangePasswordForm{},
// 			ContextValues:  ctxValues,
// 			Handler:        accountAPI.ChangePassword,
// 		},
// 		{
// 			TestName:       "changePasswordTest 3 POST 200 response",
// 			Method:         "POST",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     requestURL,
// 			ExpectedStatus: http.StatusOK,
// 			Form:           form,
// 			ContextValues:  ctxValues,
// 			ScannerMap: map[string]ScannerReturn{
// 				"select password from user_profile": ScannerReturn{
// 					scanValues: []interface{}{
// 						config.HashPassword,
// 					},
// 				},
// 			},
// 			Handler: accountAPI.ChangePassword,
// 		},
// 	}

// 	runTestCases(t, testCases)
// }

// func verificationTest(
// 	t *testing.T,
// 	mockDB httputil.DBInterface,
// 	mockCache config.CacheStore,
// 	mockStore sessions.Store,
// 	mockMessage config.SendMessage,
// ) {
// 	allowedMethods := []string{"GET"}
// 	handlerURL := "/api/account/verification/{token}/"
// 	failRequestURL := "/api/account/verification/failure/"
// 	successRequestURL := "/api/account/verification/success/"
// 	accountAPI := NewAccountAPI(mockDB, mockCache, mockStore, mockMessage)

// 	testCases := []TestCase{
// 		{
// 			TestName:       "verificationTest; GET 404 response",
// 			Method:         "GET",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     failRequestURL,
// 			ExpectedStatus: http.StatusNotFound,
// 			CustomSwitchValues: map[string]error{
// 				"cache": errQuery,
// 			},
// 			Handler: accountAPI.Verification,
// 		},
// 		{
// 			TestName:       "verificationTest; GET 200 response",
// 			Method:         "GET",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     successRequestURL,
// 			ExpectedStatus: http.StatusOK,
// 			Handler:        accountAPI.Verification,
// 		},
// 	}

// 	runTestCases(t, testCases)
// }

// func resendVerificationTest(
// 	t *testing.T,
// 	mockDB httputil.DBInterface,
// 	mockCache config.CacheStore,
// 	mockStore sessions.Store,
// 	mockMessage config.SendMessage,
// ) {
// 	allowedMethods := []string{"GET", "POST"}
// 	handlerURL := "/api/account/resend-verification/"
// 	requestURL := "/api/account/resend-verification/"
// 	resendEmail := "ResendEmail@email.com"
// 	accountAPI := NewAccountAPI(mockDB, mockCache, mockStore, mockMessage)

// 	form := &forms.EmailForm{
// 		Email: resendEmail,
// 	}

// 	switch mockDB.(type) {
// 	case *models.DB:
// 		_, err := models.QueryUserProfile(
// 			mockDB,
// 			"select id from user_profile where email = $1;",
// 			resendEmail,
// 		)

// 		if err == sql.ErrNoRows {
// 			user := &models.UserProfile{
// 				AreaID:      2,
// 				Password:    config.HashPassword,
// 				AdminAreaID: nil,
// 				FirstName:   "Resend",
// 				LastName:    "Email",
// 				FullName:    "Resend Email",
// 				Email:       resendEmail,
// 				DateJoined:  time.Now().UTC().Format(config.DateTimeLayout),
// 			}
// 			err = user.Insert(mockDB)

// 			if err != nil {
// 				t.Fatalf("insert user err: %s", err)
// 			}
// 		} else if err != nil && err != sql.ErrNoRows {
// 			t.Fatalf("query user err: %s", err)
// 		}
// 	}

// 	testCases := []TestCase{
// 		{
// 			TestName:       "resendVerificationTest; GET 200 response",
// 			Method:         "GET",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     requestURL,
// 			ExpectedStatus: http.StatusOK,
// 			Handler:        accountAPI.ResendVerification,
// 		},
// 		{
// 			TestName:       "resendVerificationTest; POST 406 response",
// 			Method:         "POST",
// 			Form:           &forms.EmailForm{},
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     requestURL,
// 			ExpectedStatus: http.StatusNotAcceptable,
// 			Handler:        accountAPI.ResendVerification,
// 		},
// 		{
// 			TestName:       "resendVerificationTest; POST 200 response",
// 			Method:         "POST",
// 			Form:           form,
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     requestURL,
// 			ExpectedStatus: http.StatusOK,
// 			Handler:        accountAPI.ResendVerification,
// 		},
// 	}

// 	runTestCases(t, testCases)
// }

// func resetPasswordTest(
// 	t *testing.T,
// 	mockDB httputil.DBInterface,
// 	mockCache config.CacheStore,
// 	mockStore sessions.Store,
// 	mockMessage config.SendMessage,
// ) {
// 	allowedMethods := []string{"GET", "POST"}
// 	handlerURL := "/api/account/reset-password/"
// 	requestURL := "/api/account/reset-password/"
// 	resetEmail := "ResetEmail@email.com"
// 	accountAPI := NewAccountAPI(mockDB, mockCache, mockStore, mockMessage)

// 	form := &forms.EmailForm{
// 		Email: resetEmail,
// 	}

// 	switch mockDB.(type) {
// 	case *models.DB:
// 		_, err := models.QueryUserProfile(
// 			mockDB,
// 			"select id from user_profile where email = $1;",
// 			resetEmail,
// 		)

// 		if err == sql.ErrNoRows {
// 			user := &models.UserProfile{
// 				AreaID:      2,
// 				Password:    config.HashPassword,
// 				AdminAreaID: nil,
// 				FirstName:   "Reset",
// 				LastName:    "Email",
// 				FullName:    "Reset Email",
// 				Email:       resetEmail,
// 				DateJoined:  time.Now().UTC().Format(config.DateTimeLayout),
// 			}
// 			err = user.Insert(mockDB)

// 			if err != nil {
// 				t.Fatalf("insert user err: %s", err)
// 			}
// 		} else if err != nil && err != sql.ErrNoRows {
// 			t.Fatalf("query user err: %s", err)
// 		}
// 	}

// 	testCases := []TestCase{
// 		{
// 			TestName:       "resetPasswordTest; GET 200 response",
// 			Method:         "GET",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     requestURL,
// 			ExpectedStatus: http.StatusOK,
// 			Handler:        accountAPI.ResetPassword,
// 		},
// 		{
// 			TestName:       "resetPasswordTest; POST 406 response",
// 			Method:         "POST",
// 			Form:           &forms.EmailForm{},
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     requestURL,
// 			ExpectedStatus: http.StatusNotAcceptable,
// 			Handler:        accountAPI.ResetPassword,
// 		},
// 		{
// 			TestName:       "resetPasswordTest; POST 200 response",
// 			Method:         "POST",
// 			Form:           form,
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     requestURL,
// 			ExpectedStatus: http.StatusOK,
// 			Handler:        accountAPI.ResetPassword,
// 		},
// 	}

// 	runTestCases(t, testCases)
// }

// func confirmPasswordResetTest(
// 	t *testing.T,
// 	mockDB httputil.DBInterface,
// 	mockCache config.CacheStore,
// 	mockStore sessions.Store,
// 	mockMessage config.SendMessage,
// ) {
// 	allowedMethods := []string{"GET", "POST"}
// 	handlerURL := "/api/account/confirm-password-reset/{token}/"
// 	failRequestURL := "/api/account/confirm-password-reset/failure/"
// 	successRequestURL := "/api/account/confirm-password-reset/success/"
// 	//emailRequestURL := "/api/account/confirm-password-reset/email/"
// 	confirmEmail := "confirmEmail@email.com"
// 	accountAPI := NewAccountAPI(mockDB, mockCache, mockStore, mockMessage)

// 	form1 := &forms.ConfirmPasswordForm{}
// 	form2 := &forms.ConfirmPasswordForm{
// 		NewPassword:     "newpassword",
// 		ConfirmPassword: "newpassword",
// 	}

// 	switch mockDB.(type) {
// 	case *models.DB:
// 		_, err := models.QueryUserProfile(
// 			mockDB,
// 			"select * from user_profile where email = $1;",
// 			confirmEmail,
// 		)

// 		if err == sql.ErrNoRows {
// 			user := &models.UserProfile{
// 				AreaID:      2,
// 				Password:    config.HashPassword,
// 				AdminAreaID: nil,
// 				FirstName:   "Confirm",
// 				LastName:    "Password",
// 				FullName:    "Confirm Password",
// 				Email:       confirmEmail,
// 				DateJoined:  time.Now().UTC().Format(config.DateTimeLayout),
// 			}
// 			err = user.Insert(mockDB)

// 			if err != nil {
// 				t.Fatalf("insert user err: %s", err)
// 			}

// 		} else if err != nil && err != sql.ErrNoRows {
// 			t.Fatalf("query user err: %s", err)
// 		}
// 	}

// 	testCases := []TestCase{
// 		{
// 			TestName:       "confirmPasswordResetTest 1 GET 404 response",
// 			Method:         "GET",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     failRequestURL,
// 			ExpectedStatus: http.StatusNotFound,
// 			Handler:        accountAPI.ConfirmPasswordReset,
// 		},
// 		{
// 			TestName:       "confirmPasswordResetTest 2 GET 200 response",
// 			Method:         "GET",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     successRequestURL,
// 			ExpectedStatus: http.StatusOK,
// 			// CustomSwitchValues: map[string]error{
// 			// 	"token": errQuery,
// 			// },
// 			Handler: accountAPI.ConfirmPasswordReset,
// 		},
// 		{
// 			TestName:       "confirmPasswordResetTest 3 POST 406 response",
// 			Method:         "POST",
// 			Form:           form1,
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     successRequestURL,
// 			ExpectedStatus: http.StatusNotAcceptable,
// 			CustomSwitchValues: map[string]error{
// 				"cache": errQuery,
// 			},
// 			Handler: accountAPI.ConfirmPasswordReset,
// 		},
// 		{
// 			TestName:       "confirmPasswordResetTest 4 POST 200 response",
// 			Method:         "POST",
// 			Form:           form2,
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     successRequestURL,
// 			ExpectedStatus: http.StatusOK,
// 			CacheValue:     confirmEmail,
// 			Handler:        accountAPI.ConfirmPasswordReset,
// 		},
// 	}

// 	runTestCases(t, testCases)
// }

// func emailExistsTest(
// 	t *testing.T,
// 	mockDB httputil.DBInterface,
// 	mockCache config.CacheStore,
// 	mockStore sessions.Store,
// 	mockMessage config.SendMessage,
// ) {
// 	allowedMethods := []string{"GET"}
// 	handlerURL := "/api/account/email-exists/{email}/"
// 	exisitngEmail := "ExistingEmail@email.com"
// 	existsRequestURL := "/api/account/email-exists/" + exisitngEmail + "/"
// 	doesNotExistRequestURL := "/api/account/email-exists/ffdgjkgjk@email.com/"
// 	accountAPI := NewAccountAPI(mockDB, mockCache, mockStore, mockMessage)

// 	switch mockDB.(type) {
// 	case *models.DB:
// 		_, err := models.QueryUserProfile(
// 			mockDB,
// 			"select id from user_profile where email = $1;",
// 			exisitngEmail,
// 		)

// 		if err == sql.ErrNoRows {
// 			user := &models.UserProfile{
// 				AreaID:      2,
// 				Password:    config.HashPassword,
// 				AdminAreaID: nil,
// 				FirstName:   "Exisitng",
// 				LastName:    "Email",
// 				FullName:    "Existing Email",
// 				Email:       exisitngEmail,
// 				DateJoined:  time.Now().UTC().Format(config.DateTimeLayout),
// 			}
// 			err = user.Insert(mockDB)

// 			if err != nil {
// 				t.Fatalf("insert user err: %s", err)
// 			}

// 		} else if err != nil && err != sql.ErrNoRows {
// 			t.Fatalf("query user err: %s", err)
// 		}
// 	}

// 	testCases := []TestCase{
// 		{
// 			TestName:       "emailExistsTest 1 GET 200 response",
// 			Method:         "GET",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     existsRequestURL,
// 			ExpectedStatus: http.StatusOK,
// 			ExpectedBody:   "{\"uniqueEmail\":false}",
// 			Handler:        accountAPI.EmailExists,
// 		},
// 		{
// 			TestName:       "emailExistsTest 2 GET 200 response",
// 			Method:         "GET",
// 			AllowedMethods: allowedMethods,
// 			HandlerURL:     handlerURL,
// 			RequestURL:     doesNotExistRequestURL,
// 			ExpectedStatus: http.StatusOK,
// 			CustomSwitchValues: map[string]error{
// 				"user": sql.ErrNoRows,
// 			},
// 			ExpectedBody: "{\"uniqueEmail\":true}",
// 			Handler:      accountAPI.EmailExists,
// 		},
// 	}

// 	runTestCases(t, testCases)
// }

// // -------------------------- UNIT TESTS ---------------------------------

// // func TestLoggedInUnitTest(t *testing.T) {
// // 	loggedInTest(t, mockAccountDB, mockCache, mockStore, mockMessage)
// // }

// // func TestEmailExistsUnitTest(t *testing.T) {
// // 	emailExistsTest(t, mockAccountDB, mockCache, mockStore, mockMessage)
// // }

// // func TestConfirmPasswordUnitTest(t *testing.T) {
// // 	confirmPasswordResetTest(t, mockAccountDB, mockCache, mockStore, mockMessage)
// // }

// // func TestResetPasswordUnitTest(t *testing.T) {
// // 	resetPasswordTest(t, mockAccountDB, mockCache, mockStore, mockMessage)
// // }

// // func TestResendVerificationUnitTest(t *testing.T) {
// // 	resendVerificationTest(t, mockAccountDB, mockCache, mockStore, mockMessage)
// // }

// // func TestVerificationUnitTest(t *testing.T) {
// // 	verificationTest(t, mockAccountDB, mockCache, mockStore, mockMessage)
// // }

// // func TestChangePasswordUnitTest(t *testing.T) {
// // 	changePasswordTest(t, mockAccountDB, mockCache, mockStore, mockMessage)
// // }

// // func TestLoginUnitTest(t *testing.T) {
// // 	loginTest(t, mockAccountDB, mockCache, mockStore, mockMessage)
// // }

// // func TestAccountAPIUnitTests(t *testing.T) {
// // 	loggedInTest(t, mockAccountDB, mockCache, mockStore, mockMessage)
// // 	emailExistsTest(t, mockAccountDB, mockCache, mockStore, mockMessage)
// // 	confirmPasswordResetTest(t, mockAccountDB, mockCache, mockStore, mockMessage)
// // 	resetPasswordTest(t, mockAccountDB, mockCache, mockStore, mockMessage)
// // 	resendVerificationTest(t, mockAccountDB, mockCache, mockStore, mockMessage)
// // 	loginTest(t, mockAccountDB, mockCache, mockStore, mockMessage)
// // }

// // -------------------------- INTEGRATION TESTS ---------------------------------

// func TestLoggedInIntegrationTest(t *testing.T) {
// 	loggedInTest(t, testDB, mockCache, mockStore, mockMessage)
// }

// func TestEmailExistsIntegrationTest(t *testing.T) {
// 	emailExistsTest(t, testDB, mockCache, mockStore, mockMessage)
// }

// func TestConfirmPasswordIntegrationTest(t *testing.T) {
// 	confirmPasswordResetTest(t, testDB, mockCache, mockStore, mockMessage)
// }

// func TestResetPasswordIntegrationTest(t *testing.T) {
// 	resetPasswordTest(t, testDB, mockCache, mockStore, mockMessage)
// }

// func TestResendVerificationIntegrationTest(t *testing.T) {
// 	resendVerificationTest(t, testDB, mockCache, mockStore, mockMessage)
// }

// func TestVerificationIntegrationTest(t *testing.T) {
// 	verificationTest(t, testDB, mockCache, mockStore, mockMessage)
// }

// func TestChangePasswordIntegrationTest(t *testing.T) {
// 	changePasswordTest(t, testDB, mockCache, mockStore, mockMessage)
// }

// func TestLoginIntegrationTest(t *testing.T) {
// 	loginTest(t, testDB, mockCache, mockStore, mockMessage)
// }

// // func TestAccountAPIIntegrationTests(t *testing.T) {
// // 	loggedInTest(t, testDB, mockCache, mockStore, mockMessage)
// // 	loginTest(t, testDB, mockCache, mockStore, mockMessage)
// // 	changePasswordTest(t, testDB, mockCache, mockStore, mockMessage)
// // 	verificationTest(t, testDB, mockCache, mockStore, mockMessage)
// // 	resendVerificationTest(t, testDB, mockCache, mockStore, mockMessage)
// // 	resetPasswordTest(t, testDB, mockCache, mockStore, mockMessage)
// // 	confirmPasswordResetTest(t, testDB, mockCache, mockStore, mockMessage)
// // 	emailExistsTest(t, testDB, mockCache, mockStore, mockMessage)
// // }
