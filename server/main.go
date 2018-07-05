package main

import (
	"fmt"
	"net/http"

	"github.com/gorilla/mux"

	"github.com/TravisS25/httputil/apiutil"
	"github.com/TravisS25/inventory-tracking/src/server/api"
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
	// areaAPI := api.NewAreaAPI(config.DB)
	// contractorAPI := api.NewContractorAPI(config.DB)
	// userAPI := api.NewUserProfileAPI(config.DB, config.Mailer)
	// noteAPI := api.NewNoteAPI(config.DB)
	// foundFromAPI := api.NewFoundFromAPI(config.DB)
	// callStatusAPI := api.NewCallStatusAPI(config.DB)
	// rejectionReasonAPI := api.NewRejectionReasonAPI(config.DB)

	// Account API
	r.HandleFunc("/api/account/login/", accountAPI.Login).Methods("GET", "POST")
	r.HandleFunc("/api/account/logout/", accountAPI.Logout).Methods("GET")
	r.HandleFunc("/api/account/user/details/", accountAPI.AccountDetails).Methods("GET")
	r.HandleFunc("/api/account/change-password/", accountAPI.ChangePassword).Methods("GET", "POST", "OPTIONS")
	r.HandleFunc("/api/account/reset-password/", accountAPI.ResetPassword).Methods("GET", "POST")
	r.HandleFunc("/api/account/confirm-password-reset/{token}/", accountAPI.ConfirmPasswordReset).Methods("GET", "POST", "OPTIONS")
	r.HandleFunc("/api/account/email-exists/{email}/", accountAPI.EmailExists).Methods("GET")

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
