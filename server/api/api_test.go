package api

import (
	"github.com/TravisS25/httputil/formutil"
	"github.com/TravisS25/inventory-tracking/src/server/config"
	"github.com/TravisS25/inventory-tracking/src/server/forms"
	"github.com/gorilla/mux"
)

var (
	unitTestRouter = mux.NewRouter()
)

func init() {
	initUnitTestAPIs()
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
	r.HandleFunc(config.RouterPaths["userDetails"], accountAPI.AccountDetails).Methods("GET")
	r.HandleFunc(config.RouterPaths["chagePassword"], accountAPI.ChangePassword).Methods("GET", "POST", "OPTIONS")
	r.HandleFunc(config.RouterPaths["resetPassword"], accountAPI.ResetPassword).Methods("GET", "POST")
	r.HandleFunc(config.RouterPaths["confirmPasswordReset"], accountAPI.ConfirmPasswordReset).Methods("GET", "POST", "OPTIONS")
	r.HandleFunc(config.RouterPaths["emailExists"], accountAPI.EmailExists).Methods("GET")

	// Machine API
	r.HandleFunc(config.RouterPaths["machineSearch"], machineAPI.MachineSearch).Methods("GET")
	r.HandleFunc(config.RouterPaths["machineUpload"], machineAPI.MachineUpload).Methods("GET", "POST")
	r.HandleFunc(config.RouterPaths["machineDetails"], machineAPI.MachineDetails).Methods("GET")
	r.HandleFunc(config.RouterPaths["machineSwap"], machineAPI.MachineSwap).Methods("GET", "PUT")
	r.HandleFunc(config.RouterPaths["machineEdit"], machineAPI.MachineEdit).Methods("GET", "PUT")
}
