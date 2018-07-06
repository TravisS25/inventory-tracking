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
	//filterParams := []string{"skip", "{skip:[0-9]+}", "take", "{take:[0-9]+}"}

	// ------------------------------ APIs -------------------------------------

	accountAPI := api.NewAccountAPI(config.DB, config.Cache, config.SessionStore, config.Mailer)
	machineAPI := api.NewMachineAPI(config.DB, config.Cache, map[string]formutil.Form{
		"form":     forms.NewMachineValidator(config.FormValidation),
		"formSwap": forms.NewMachineSwapValidator(config.FormValidation),
	})

	// Account API
	r.HandleFunc(config.RouterPaths["login"], accountAPI.Login).Methods("GET", "POST")
	r.HandleFunc(config.RouterPaths["logout"], accountAPI.Logout).Methods("GET")
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

	middleware := apiutil.NewMiddleware(config.SessionStore, config.Cache, []string{})

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
		negroni.NewLogger(),
		c,
	)

	n.UseHandler(r)

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
