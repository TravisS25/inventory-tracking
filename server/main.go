package main

import (
	"fmt"
	"net/http"

	"bitbucket.org/TravisS25/contractor-tracking/contractor-tracking/contractor-server/api"

	"github.com/rs/cors"
	"github.com/urfave/negroni"

	"bitbucket.org/TravisS25/contractor-tracking/contractor-tracking/contractor-server/config"
)

func main() {
	fmt.Println("Starting Server...")
	filterParams := []string{"skip", "{skip:[0-9]+}", "take", "{take:[0-9]+}"}

	// ------------------------------ APIs -------------------------------------

	accountAPI := api.NewAccountAPI(config.DB, config.Cache, config.Store, config.Mailer)
	areaAPI := api.NewAreaAPI(config.DB)
	contractorAPI := api.NewContractorAPI(config.DB)
	userAPI := api.NewUserProfileAPI(config.DB, config.Mailer)
	noteAPI := api.NewNoteAPI(config.DB)
	foundFromAPI := api.NewFoundFromAPI(config.DB)
	callStatusAPI := api.NewCallStatusAPI(config.DB)
	rejectionReasonAPI := api.NewRejectionReasonAPI(config.DB)

	// Account API
	r.HandleFunc("/api/account/login/", accountAPI.Login).Methods("GET", "POST")
	//r.HandleFunc("/api/account/logged-in/", accountAPI.LoggedIn).Methods("GET")
	r.HandleFunc("/api/account/logout/", accountAPI.Logout).Methods("GET")
	r.HandleFunc("/api/account/user/details/", accountAPI.AccountDetails).Methods("GET")
	r.HandleFunc("/api/account/change-password/", accountAPI.ChangePassword).Methods("GET", "POST", "OPTIONS")
	// r.HandleFunc("/api/account/verification/{token}/", accountAPI.Verification).Methods("GET")
	// r.HandleFunc("/api/account/resend-verification/", accountAPI.ResendVerification).Methods("GET", "POST")
	r.HandleFunc("/api/account/reset-password/", accountAPI.ResetPassword).Methods("GET", "POST")
	r.HandleFunc("/api/account/confirm-password-reset/{token}/", accountAPI.ConfirmPasswordReset).Methods("GET", "POST", "OPTIONS")
	r.HandleFunc("/api/account/email-exists/{email}/", accountAPI.EmailExists).Methods("GET")

	// Init middleware
	c := cors.New(cors.Options{
		AllowedOrigins:   config.Conf.AllowedOrigins,
		AllowCredentials: true,
		AllowedMethods:   []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"},
		AllowedHeaders:   []string{"*"},
		ExposedHeaders:   []string{"*"},
	})
	n := negroni.New(
		negroni.HandlerFunc(api.AuthMiddleware),
		negroni.HandlerFunc(api.RoutingMiddleware),
		negroni.HandlerFunc(api.GroupMiddleware),
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
