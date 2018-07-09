package api

import (
	"bytes"
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"

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
	TestPassword     = "Password123!"
	TestPasswordHash = "$2a$10$bi8mFKrlUfYlXgeIJj6buucEgT0scC./LaMAqOfnAAHMEcTPaXqy2"
	TestEmail        = "testemail@email.com"
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
	accountAPI := NewAccountAPI(TestDB, config.Cache, config.SessionStore, TestMailer, map[string]formutil.Validator{
		"loginForm": forms.LoginValidator{FormValidation: TestFormValidation},
	})
	machineAPI := NewMachineAPI(TestDB, config.Cache, map[string]formutil.Validator{
		"form":     forms.NewMachineValidator(TestFormValidation),
		"formSwap": forms.NewMachineSwapValidator(TestFormValidation),
	})

	// Account API
	IntegrationTestRouter.HandleFunc(config.RouterPaths["login"], accountAPI.Login).Methods("GET", "POST")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["logout"], accountAPI.Logout).Methods("GET")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["userDetails"], accountAPI.AccountDetails).Methods("GET")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["chagePassword"], accountAPI.ChangePassword).Methods("GET", "POST", "OPTIONS")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["resetPassword"], accountAPI.ResetPassword).Methods("GET", "POST")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["confirmPasswordReset"], accountAPI.ConfirmPasswordReset).Methods("GET", "POST", "OPTIONS")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["emailExists"], accountAPI.EmailExists).Methods("GET")

	// Machine API
	IntegrationTestRouter.HandleFunc(config.RouterPaths["machineSearch"], machineAPI.MachineSearch).Methods("GET")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["machineUpload"], machineAPI.MachineUpload).Methods("GET", "POST")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["machineDetails"], machineAPI.MachineDetails).Methods("GET")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["machineSwap"], machineAPI.MachineSwap).Methods("GET", "PUT")
	IntegrationTestRouter.HandleFunc(config.RouterPaths["machineEdit"], machineAPI.MachineEdit).Methods("GET", "PUT")
}

func initUnitTestAPIs() {
	accountAPI := NewAccountAPI(config.DB, config.Cache, config.SessionStore, config.Mailer, map[string]formutil.Validator{
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
	UnitTestRouter.HandleFunc(config.RouterPaths["chagePassword"], accountAPI.ChangePassword).Methods("GET", "POST", "OPTIONS")
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
	// middleware := apiutil.NewMiddleware(config.SessionStore, config.Cache, TestDB, []string{
	// 	config.RouterPaths["login"],
	// 	config.RouterPaths["resetPassword"],
	// 	config.RouterPaths["confirmPasswordReset"],
	// })

	middleware := apiutil.Middleware{
		CacheStore:      config.Cache,
		SessionStore:    config.SessionStore,
		DB:              TestDB,
		Inserter:        models.LogEntry{},
		UserSessionName: "user",
		AnonRouting: []string{
			config.RouterPaths["login"],
			config.RouterPaths["resetPassword"],
			config.RouterPaths["confirmPasswordReset"],
		},
	}
	recoverHandler := negroni.NewRecovery()
	recoverHandler.PanicHandlerFunc = func(info *negroni.PanicInformation) {
		message := &mailutil.Message{}
		message.SetMessage(info.StackAsString())
		err := TestMailer.Send(message)

		if err != nil {
			fmt.Printf("sending mail error: %s", err.Error())
		}
	}

	// Init middleware
	c := cors.New(cors.Options{
		AllowedOrigins:   config.Conf.AllowedOrigins,
		AllowCredentials: true,
		AllowedMethods:   []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowedHeaders:   []string{"*"},
		ExposedHeaders:   []string{"*"},
	})
	n := negroni.New(
		negroni.HandlerFunc(middleware.AuthMiddleware),
		negroni.HandlerFunc(middleware.GroupMiddleware),
		negroni.HandlerFunc(middleware.RoutingMiddleware),
		negroni.HandlerFunc(middleware.LogEntryMiddleware),
		negroni.NewLogger(),
		recoverHandler,
		c,
	)

	n.UseHandler(config.CSRF(IntegrationTestRouter))
	return n
}

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

	fmt.Printf("headers get: %s", res.Header)
	token = res.Header.Get("X-CSRF-TOKEN")
	csrf = res.Header.Get("Set-Cookie")
	fmt.Printf("token recieved: %s\n", token)
	fmt.Printf("csrf recieved: %s\n", csrf)

	//t.Errorf("response error: %d", res.StatusCode)

	buffer = apiutil.GetJSONBuffer(forms.LoginForm{
		Email:    TestEmail,
		Password: TestPassword,
	})
	req, err = http.NewRequest("POST", loginURL, &buffer)

	if err != nil {
		t.Fatal("err on request")
	}

	req.Header.Set("X-CSRF-TOKEN", token)
	req.Header.Set("Cookie", csrf)
	res, err = client.Do(req)

	if err != nil {
		t.Fatal("err on response")
	}
	//buffer.Reset()

	// fmt.Println(res.Header)
	fmt.Println(res.StatusCode)
	t.Error("hi")
}

func TestLogin2(t *testing.T) {
	var req *http.Request
	var res *http.Response
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

	res, err = client.Do(req)

	if err != nil {
		t.Fatal("err on response")
	}

	fmt.Printf("headers get: %s", res.Header)
	t.Error("hi")
}