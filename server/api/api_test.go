package api

import (
	"bytes"
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/TravisS25/httputil/confutil"
	"github.com/TravisS25/httputil/queryutil"
	"github.com/satori/go.uuid"

	"github.com/TravisS25/httputil/apiutil"
	"github.com/TravisS25/httputil/dbutil"
	"github.com/TravisS25/httputil/formutil"
	"github.com/TravisS25/httputil/mailutil"
	"github.com/TravisS25/inventory-tracking/src/server/config"
	"github.com/TravisS25/inventory-tracking/src/server/forms"
	"github.com/TravisS25/inventory-tracking/src/server/models"
	"github.com/gorilla/mux"
	"github.com/rs/cors"
	"github.com/urfave/negroni"
)

const (
	WorkerEmail      = "worker@email.com"
	AdminEmail       = "admin@email.com"
	TestPassword     = "Password123!"
	TestPasswordHash = "$2a$10$bi8mFKrlUfYlXgeIJj6buucEgT0scC./LaMAqOfnAAHMEcTPaXqy2"
	TestEmail        = "testemail@email.com"
	SetCookieHeader  = "Set-Cookie"
	CookieHeader     = "Cookie"
	TokenHeader      = "X-CSRF-TOKEN"
)

var (
	UnitTestRouter        = mux.NewRouter()
	IntegrationTestRouter = mux.NewRouter()
	TestFormValidation    = formutil.FormValidation{}
	TestDB                *dbutil.DB
	TestMailer            mailutil.SendMessage
)

func init() {
	initDB()
	initMailer()
	initFormValidation()
	initUnitTestAPIs()
	initIntegrationTestAPIs()
}

func initFormValidation() {
	TestFormValidation.SetQuerier(TestDB)
	TestFormValidation.SetCache(config.Cache)
}

func initMailer() {
	TestMailer = mailutil.NewMailMessenger(mailutil.MailerConfig{
		Host:     config.Conf.EmailConfig.TestEmail.Host,
		Port:     config.Conf.EmailConfig.TestEmail.Port,
		User:     config.Conf.EmailConfig.TestEmail.User,
		Password: config.Conf.EmailConfig.TestEmail.Password,
	})
}

func initDB() {
	var err error
	fmt.Println(config.Conf.DatabaseConfig.Test.DBName)
	TestDB, err = dbutil.NewDB(dbutil.DBConfig{
		Host:     config.Conf.DatabaseConfig.Test.Host,
		User:     config.Conf.DatabaseConfig.Test.User,
		Password: config.Conf.DatabaseConfig.Test.Password,
		DBName:   config.Conf.DatabaseConfig.Test.DBName,
		Port:     config.Conf.DatabaseConfig.Test.Port,
		SSLMode:  config.Conf.DatabaseConfig.Test.SSlMode,
	})

	if err != nil {
		panic(err)
	}
}

func initIntegrationTestAPIs() {
	accountAPI := NewAccountAPI(
		TestDB,
		config.Cache,
		config.SessionStore,
		TestMailer,
		config.Conf.Prod,
		map[string]formutil.Validator{
			"loginForm":           forms.NewLoginValidator(TestFormValidation),
			"changePasswordForm":  forms.NewChangePasswordValidator(TestFormValidation),
			"confirmPasswordForm": forms.NewConfirmPasswordValidator(TestFormValidation),
			"emailForm":           forms.NewEmailValidator(TestFormValidation),
		})
	machineAPI := NewMachineAPI(TestDB, config.Cache, map[string]formutil.Validator{
		"form":     forms.NewMachineValidator(TestFormValidation),
		"formSwap": forms.NewMachineSwapValidator(TestFormValidation),
	})
	loggingAPI := NewLoggingAPI(TestDB)

	// Account API
	IntegrationTestRouter.HandleFunc(config.RouterPaths["login"], accountAPI.Login).Methods("GET", "POST")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["logout"], accountAPI.Logout).Methods("GET")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["userDetails"], accountAPI.AccountDetails).Methods("GET")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["changePassword"], accountAPI.ChangePassword).Methods("GET", "POST", "OPTIONS")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["resetPassword"], accountAPI.ResetPassword).Methods("GET", "POST")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["confirmPasswordReset"], accountAPI.ConfirmPasswordReset).Methods("GET", "POST", "OPTIONS")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["emailExists"], accountAPI.EmailExists).Methods("GET")

	// Machine API
	IntegrationTestRouter.HandleFunc(config.RouterPaths["machineSearch"], machineAPI.MachineSearch).Methods("GET")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["machineUpload"], machineAPI.MachineUpload).Methods("GET", "POST")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["machineDetails"], machineAPI.MachineDetails).Methods("GET")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["machineSwap"], machineAPI.MachineSwap).Methods("GET", "PUT")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["machineEdit"], machineAPI.MachineEdit).Methods("GET", "PUT")

	// Logging API
	IntegrationTestRouter.HandleFunc(config.RouterPaths["logIndex"], loggingAPI.Index).Methods("GET")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["logCountIndex"], loggingAPI.CountIndex).Methods("GET")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["logDetails"], loggingAPI.LogDetails).Methods("GET")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["logRowDetails"], loggingAPI.RowDetails).Methods("GET")
}

func initUnitTestAPIs() {
	accountAPI := NewAccountAPI(
		TestDB,
		config.Cache,
		config.SessionStore,
		config.Mailer,
		config.Conf.Prod,
		map[string]formutil.Validator{
			"loginForm": forms.LoginValidator{FormValidation: TestFormValidation},
		})
	machineAPI := NewMachineAPI(config.DB, config.Cache, map[string]formutil.Validator{
		"form":     forms.NewMachineValidator(config.FormValidation),
		"formSwap": forms.NewMachineSwapValidator(config.FormValidation),
	})

	// Account API
	UnitTestRouter.HandleFunc(config.RouterPaths["login"], accountAPI.Login).Methods("GET", "POST")
	UnitTestRouter.HandleFunc(config.RouterPaths["logout"], accountAPI.Logout).Methods("GET")
	UnitTestRouter.HandleFunc(config.RouterPaths["userDetails"], accountAPI.AccountDetails).Methods("GET")
	UnitTestRouter.HandleFunc(config.RouterPaths["changePassword"], accountAPI.ChangePassword).Methods("GET", "POST", "OPTIONS")
	UnitTestRouter.HandleFunc(config.RouterPaths["resetPassword"], accountAPI.ResetPassword).Methods("GET", "POST")
	UnitTestRouter.HandleFunc(config.RouterPaths["confirmPasswordReset"], accountAPI.ConfirmPasswordReset).Methods("GET", "POST", "OPTIONS")
	UnitTestRouter.HandleFunc(config.RouterPaths["emailExists"], accountAPI.EmailExists).Methods("GET")

	// Machine API
	UnitTestRouter.HandleFunc(config.RouterPaths["machineSearch"], machineAPI.MachineSearch).Methods("GET")
	UnitTestRouter.HandleFunc(config.RouterPaths["machineUpload"], machineAPI.MachineUpload).Methods("GET", "POST")
	UnitTestRouter.HandleFunc(config.RouterPaths["machineDetails"], machineAPI.MachineDetails).Methods("GET")
	UnitTestRouter.HandleFunc(config.RouterPaths["machineSwap"], machineAPI.MachineSwap).Methods("GET", "PUT")
	UnitTestRouter.HandleFunc(config.RouterPaths["machineEdit"], machineAPI.MachineEdit).Methods("GET", "PUT")
}

func App() http.Handler {
	middleware := apiutil.Middleware{
		CacheStore:      config.Cache,
		SessionStore:    config.SessionStore,
		DB:              TestDB,
		LogInserter:     config.LogInserter,
		UserSessionName: "user",
		AnonRouting: []string{
			config.RouterPaths["login"],
			config.RouterPaths["resetPassword"],
			"api/account/confirm-password-reset",
		},
	}
	recoverHandler := negroni.NewRecovery()
	recoverHandler.PanicHandlerFunc = apiutil.PanicHandlerFunc(
		[]string{"admin@email.com"},
		"admin@email.com",
		"500 Panic Error",
		nil,
		//[]string{"TravisS25"},
		TestMailer,
	)

	// Init middleware
	c := cors.New(cors.Options{
		AllowedOrigins:   config.Conf.AllowedOrigins,
		AllowCredentials: true,
		AllowedMethods:   []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowedHeaders:   []string{"*"},
		ExposedHeaders:   []string{"*"},
	})
	n := negroni.New(
		recoverHandler,
		negroni.HandlerFunc(middleware.AuthMiddleware),
		negroni.HandlerFunc(middleware.GroupMiddleware),
		negroni.HandlerFunc(middleware.RoutingMiddleware),
		negroni.HandlerFunc(middleware.LogEntryMiddleware),
		negroni.NewLogger(),
		c,
	)

	n.UseHandler(config.CSRF(IntegrationTestRouter))
	return n
}

func loginUser(email, password string, ts *httptest.Server) (string, error) {
	client := &http.Client{}

	baseURL := ts.URL
	loginURL := baseURL + config.RouterPaths["login"]

	req, err := http.NewRequest("GET", loginURL, nil)

	if err != nil {
		return "", err
	}

	res, err := client.Do(req)

	if err != nil {
		return "", err
	}

	token := res.Header.Get(TokenHeader)
	csrf := res.Header.Get(SetCookieHeader)
	buffer := apiutil.GetJSONBuffer(forms.LoginForm{
		Email:    email,
		Password: password,
	})
	req, err = http.NewRequest("POST", loginURL, &buffer)

	if err != nil {
		return "", err
	}

	req.Header.Set(TokenHeader, token)
	req.Header.Set(CookieHeader, csrf)
	res, err = client.Do(req)

	if err != nil {
		return "", err
	}

	return res.Header.Get(SetCookieHeader), nil
}

func TestLoggerInsert(t *testing.T) {
	id, _ := uuid.NewV4()
	form := queryutil.GeneralJSON{
		"password": "Password123!",
		"email":    "worker@email.com",
	}

	currentTime := time.Now().UTC().Format(confutil.DateTimeLayout)
	logger := models.LoggingHistory{
		ID:          id,
		DateEntered: &currentTime,
		APIURL:      "/api/account/login/",
		Operation:   models.HTTPOperationPost,
		JSONData:    form,
	}

	fmt.Println(logger)
	err := logger.Insert(TestDB)

	if err != nil {
		t.Error(err)
	}

	machine := models.Machine{
		MachineName:     "Foo",
		RoomID:          1,
		MachineStatusID: 1,
		ScannedTime:     time.Now().UTC().Format(confutil.DateTimeLayout),
	}

	err = machine.Insert(TestDB)

	if err != nil {
		fmt.Printf("machine err: %s", err.Error())
	}
}

// func TestJSON(t *testing.T) {
// 	logs, err := models.QueryLoggingHistories(
// 		TestDB,
// 		"select * from logging_history",
// 	)

// 	if err != nil {
// 		t.Fatalf("query err: %s", err.Error())
// 	}

// 	for _, v := range logs {
// 		for k, m := range v.JSONData {
// 			if k == "array" {
// 				newM := m.([]interface{})

// 				for _, boom := range newM {
// 					conv := boom.(map[string]interface{})

// 					for l, p := range conv {
// 						t.Errorf("inner key: %s\n", l)
// 						t.Errorf("inner value: %s\n", p)
// 					}
// 				}
// 			}

// 			t.Errorf("key: %s\n", k)
// 			t.Errorf("value: %s\n", m)
// 		}
// 	}
// }

// ------------------- END TO END TESTING ---------------------

func TestApp(t *testing.T) {
	var req *http.Request
	var res *http.Response
	var err error
	var buffer bytes.Buffer
	var token string
	var csrf string

	ts := httptest.NewServer(App())
	defer ts.Close()

	client := &http.Client{}

	baseURL := ts.URL
	loginURL := baseURL + config.RouterPaths["login"]
	//machineUploadURL := baseURL + config.RouterPaths["machineUpload"]
	// machineSearchURL := baseURL + config.RouterPaths["machineSearch"]
	// machineDetailsURL := baseURL + config.RouterPaths["machineDetails"]
	// machineSwapURL := baseURL + config.RouterPaths["machineSwap"]
	// machineEdit := baseURL + config.RouterPaths["machineEdit"]

	req, err = http.NewRequest("GET", loginURL, nil)

	if err != nil {
		t.Fatal("err on request")
	}

	res, err = client.Do(req)

	if err != nil {
		t.Fatal("err on response")
	}

	//fmt.Printf("headers get: %s", res.Header)
	token = res.Header.Get(TokenHeader)
	csrf = res.Header.Get(SetCookieHeader)
	// fmt.Printf("token recieved: %s\n", token)
	// fmt.Printf("csrf recieved: %s\n", csrf)

	//t.Errorf("response error: %d", res.StatusCode)

	buffer = apiutil.GetJSONBuffer(forms.LoginForm{
		Email:    "worker@email.com",
		Password: TestPassword,
	})
	req, err = http.NewRequest("POST", loginURL, &buffer)

	if err != nil {
		t.Fatal("err on request")
	}

	req.Header.Set(TokenHeader, token)
	req.Header.Set(CookieHeader, csrf)
	res, err = client.Do(req)

	if err != nil {
		t.Fatal("err on response")
	}

	// fmt.Printf("user cookie: %s\n", res.Header.Get(SetCookieHeader))

	// fmt.Println(res.Header)
	// fmt.Println(res.StatusCode)
	t.Error("hi")
}

func TestLogin2(t *testing.T) {
	var req *http.Request
	//var res *http.Response
	var err error

	ts := httptest.NewServer(App())
	defer ts.Close()

	client := &http.Client{}

	baseURL := ts.URL
	loginURL := baseURL + config.RouterPaths["login"]

	req, err = http.NewRequest("GET", loginURL, nil)

	if err != nil {
		t.Fatal("err on request")
	}

	_, err = client.Do(req)

	if err != nil {
		t.Fatal("err on response")
	}

	//fmt.Printf("headers get: %s", res.Header)
	//t.Error("hi")
}
