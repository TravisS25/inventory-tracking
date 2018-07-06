package api

import (
	"github.com/TravisS25/httputil/dbutil"
	"github.com/TravisS25/httputil/formutil"
	"github.com/TravisS25/httputil/mailutil"
	"github.com/TravisS25/inventory-tracking/src/server/config"
	"github.com/TravisS25/inventory-tracking/src/server/forms"
	"github.com/gorilla/mux"
)

var (
	unitTestRouter        = mux.NewRouter()
	integrationTestRouter = mux.NewRouter()
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
	accountAPI := NewAccountAPI(TestDB, config.Cache, config.SessionStore, TestMailer)
	machineAPI := NewMachineAPI(TestDB, config.Cache, map[string]formutil.Form{
		"form":     forms.NewMachineValidator(config.FormValidation),
		"formSwap": forms.NewMachineSwapValidator(config.FormValidation),
	})

	// Account API
	integrationTestRouter.HandleFunc(config.RouterPaths["login"], accountAPI.Login).Methods("GET", "POST")
	integrationTestRouter.HandleFunc(config.RouterPaths["logout"], accountAPI.Logout).Methods("GET")
	integrationTestRouter.HandleFunc(config.RouterPaths["userDetails"], accountAPI.AccountDetails).Methods("GET")
	integrationTestRouter.HandleFunc(config.RouterPaths["chagePassword"], accountAPI.ChangePassword).Methods("GET", "POST", "OPTIONS")
	integrationTestRouter.HandleFunc(config.RouterPaths["resetPassword"], accountAPI.ResetPassword).Methods("GET", "POST")
	integrationTestRouter.HandleFunc(config.RouterPaths["confirmPasswordReset"], accountAPI.ConfirmPasswordReset).Methods("GET", "POST", "OPTIONS")
	integrationTestRouter.HandleFunc(config.RouterPaths["emailExists"], accountAPI.EmailExists).Methods("GET")

	// Machine API
	integrationTestRouter.HandleFunc(config.RouterPaths["machineSearch"], machineAPI.MachineSearch).Methods("GET")
	integrationTestRouter.HandleFunc(config.RouterPaths["machineUpload"], machineAPI.MachineUpload).Methods("GET", "POST")
	integrationTestRouter.HandleFunc(config.RouterPaths["machineDetails"], machineAPI.MachineDetails).Methods("GET")
	integrationTestRouter.HandleFunc(config.RouterPaths["machineSwap"], machineAPI.MachineSwap).Methods("GET", "PUT")
	integrationTestRouter.HandleFunc(config.RouterPaths["machineEdit"], machineAPI.MachineEdit).Methods("GET", "PUT")
}

func initUnitTestAPIs() {
	accountAPI := NewAccountAPI(config.DB, config.Cache, config.SessionStore, config.Mailer)
	machineAPI := NewMachineAPI(config.DB, config.Cache, map[string]formutil.Form{
		"form":     forms.NewMachineValidator(config.FormValidation),
		"formSwap": forms.NewMachineSwapValidator(config.FormValidation),
	})

	// Account API
	unitTestRouter.HandleFunc(config.RouterPaths["login"], accountAPI.Login).Methods("GET", "POST")
	unitTestRouter.HandleFunc(config.RouterPaths["logout"], accountAPI.Logout).Methods("GET")
	unitTestRouter.HandleFunc(config.RouterPaths["userDetails"], accountAPI.AccountDetails).Methods("GET")
	unitTestRouter.HandleFunc(config.RouterPaths["chagePassword"], accountAPI.ChangePassword).Methods("GET", "POST", "OPTIONS")
	unitTestRouter.HandleFunc(config.RouterPaths["resetPassword"], accountAPI.ResetPassword).Methods("GET", "POST")
	unitTestRouter.HandleFunc(config.RouterPaths["confirmPasswordReset"], accountAPI.ConfirmPasswordReset).Methods("GET", "POST", "OPTIONS")
	unitTestRouter.HandleFunc(config.RouterPaths["emailExists"], accountAPI.EmailExists).Methods("GET")

	// Machine API
	unitTestRouter.HandleFunc(config.RouterPaths["machineSearch"], machineAPI.MachineSearch).Methods("GET")
	unitTestRouter.HandleFunc(config.RouterPaths["machineUpload"], machineAPI.MachineUpload).Methods("GET", "POST")
	unitTestRouter.HandleFunc(config.RouterPaths["machineDetails"], machineAPI.MachineDetails).Methods("GET")
	unitTestRouter.HandleFunc(config.RouterPaths["machineSwap"], machineAPI.MachineSwap).Methods("GET", "PUT")
	unitTestRouter.HandleFunc(config.RouterPaths["machineEdit"], machineAPI.MachineEdit).Methods("GET", "PUT")
}
