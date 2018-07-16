package main

import (
	"fmt"
	"net/http"

	"github.com/TravisS25/httputil/formutil"

	"github.com/gorilla/mux"

	"github.com/TravisS25/httputil/apiutil"
	"github.com/TravisS25/inventory-tracking/src/server/api"
	"github.com/TravisS25/inventory-tracking/src/server/forms"
	"github.com/rs/cors"
	"github.com/urfave/negroni"

	"github.com/TravisS25/inventory-tracking/src/server/config"
)

func main() {
	fmt.Println("Starting Server...")
	r := mux.NewRouter()
	filterParams := []string{"skip", "{skip:[0-9]+}", "take", "{take:[0-9]+}"}

	// ------------------------------ APIs -------------------------------------

	accountAPI := api.NewAccountAPI(
		config.DB,
		config.Cache,
		config.SessionStore,
		config.Mailer,
		config.Conf.Prod,
		map[string]formutil.Validator{
			"loginForm":           forms.NewLoginValidator(config.FormValidation),
			"changePasswordForm":  forms.NewChangePasswordValidator(config.FormValidation),
			"confirmPasswordForm": forms.NewConfirmPasswordValidator(config.FormValidation),
			"emailForm":           forms.NewEmailValidator(config.FormValidation),
		})
	machineAPI := api.NewMachineAPI(config.DB, config.Cache, map[string]formutil.Validator{
		"form":     forms.NewMachineValidator(config.FormValidation),
		"formSwap": forms.NewMachineSwapValidator(config.FormValidation),
	})
	loggingAPI := api.NewLoggingAPI(config.DB)

	// Account API
	r.HandleFunc(config.RouterPaths["login"], accountAPI.Login).Methods("GET", "POST")
	r.HandleFunc(config.RouterPaths["logout"], accountAPI.Logout).Methods("GET")
	r.HandleFunc(config.RouterPaths["userDetails"], accountAPI.AccountDetails).Methods("GET")
	r.HandleFunc(config.RouterPaths["changePassword"], accountAPI.ChangePassword).Methods("GET", "POST", "OPTIONS")
	r.HandleFunc(config.RouterPaths["resetPassword"], accountAPI.ResetPassword).Methods("GET", "POST")
	r.HandleFunc(config.RouterPaths["confirmPasswordReset"], accountAPI.ConfirmPasswordReset).Methods("GET", "POST", "OPTIONS")
	r.HandleFunc(config.RouterPaths["emailExists"], accountAPI.EmailExists).Methods("GET")

	// Machine API
	r.HandleFunc(config.RouterPaths["machineSearch"], machineAPI.MachineSearch).Queries(filterParams...).Methods("GET")
	r.HandleFunc(config.RouterPaths["machineUpload"], machineAPI.MachineUpload).Methods("GET", "POST")
	r.HandleFunc(config.RouterPaths["machineDetails"], machineAPI.MachineDetails).Methods("GET")
	r.HandleFunc(config.RouterPaths["machineSwap"], machineAPI.MachineSwap).Methods("GET", "PUT")
	r.HandleFunc(config.RouterPaths["machineEdit"], machineAPI.MachineEdit).Methods("GET", "PUT")

	// Logging API
	r.HandleFunc(config.RouterPaths["logIndex"], loggingAPI.Index).Methods("GET").Queries(filterParams...)
	r.HandleFunc(config.RouterPaths["logDetails"], loggingAPI.LogDetails).Methods("GET")
	r.HandleFunc(config.RouterPaths["logRowDetails"], loggingAPI.RowDetails).Methods("GET")

	middleware := apiutil.Middleware{
		CacheStore:      config.Cache,
		SessionStore:    config.SessionStore,
		DB:              config.DB,
		LogInserter:     config.LogInserter,
		UserSessionName: "user",
		AnonRouting: []string{
			config.RouterPaths["login"],
			config.RouterPaths["resetPassword"],
			config.RouterPaths["confirmPasswordReset"],
		},
	}
	recoverHandler := negroni.NewRecovery()
	recoverHandler.PanicHandlerFunc = apiutil.PanicHandlerFunc(
		[]string{"admin@email.com"},
		"admin@email.com",
		"500 Panic Error",
		[]string{"TravisS25"},
		config.Mailer,
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

	n.UseHandler(config.CSRF(r))

	// Set server settings
	server := &http.Server{
		Addr:    config.Conf.Domain,
		Handler: n,
	}
	err := server.ListenAndServe()

	if err != nil {
		panic(err)
	}
}
