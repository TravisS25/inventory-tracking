package api

import (
	"fmt"
	"net/http"
	"net/http/httptest"
	"strconv"
	"strings"
	"testing"

	"github.com/TravisS25/httputil/apiutil"

	"github.com/TravisS25/inventory-tracking/src/server/config"
	"github.com/TravisS25/inventory-tracking/src/server/models"
)

func TestLoggingAPIs(t *testing.T) {
	var req *http.Request
	var res *http.Response
	var err error
	var workerUserCookie string
	var adminUserCookie string

	ts := httptest.NewServer(App())
	defer ts.Close()

	workerUserCookie, err = loginUser(WorkerEmail, TestPassword, ts)
	adminUserCookie, err = loginUser(AdminEmail, TestPassword, ts)

	if err != nil {
		t.Fatal("Could not login user")
	}

	client := &http.Client{}
	baseURL := ts.URL
	logIndexURL := baseURL + config.RouterPaths["logIndex"] + "?take=20&skip=0"
	logDetailsURL := baseURL + config.RouterPaths["logDetails"]
	logRowDetails := baseURL + config.RouterPaths["logRowDetails"]

	// -----------------------------------------------------------------
	//
	// Log Index API

	req, _ = http.NewRequest("GET", logIndexURL, nil)
	req.Header.Set(CookieHeader, workerUserCookie)

	res, err = client.Do(req)
	apiutil.ResponseError(t, res, http.StatusForbidden, err)

	req.Header.Set(CookieHeader, adminUserCookie)

	res, err = client.Do(req)
	apiutil.ResponseError(t, res, http.StatusOK, err)

	// -----------------------------------------------------------------
	//
	// Log Details API

	user, err := models.QueryUserProfile(
		TestDB,
		`select * from user_profile where email = $1`,
		AdminEmail,
	)

	if err != nil {
		t.Fatal("could not query admin user")
	}

	tempURL := strings.Replace(logDetailsURL, "{userID:[0-9]+}", strconv.Itoa(user.ID), 1)
	detailsURL := strings.Replace(tempURL, "{apiURL}", "api-account-login", 1)
	fmt.Printf("details Url: %s", detailsURL)
	req, _ = http.NewRequest("GET", detailsURL, nil)
	req.Header.Set(CookieHeader, adminUserCookie)

	res, err = client.Do(req)
	apiutil.ResponseError(t, res, http.StatusOK, err)

	req.Header.Set(CookieHeader, workerUserCookie)
	res, err = client.Do(req)
	apiutil.ResponseError(t, res, http.StatusForbidden, err)

	// t.Errorf("stop")

	// -----------------------------------------------------------------
	//
	// Log Row Details API

	log, err := models.QueryLoggingHistory(
		TestDB,
		`select * from logging_history limit 1`,
	)

	if err != nil {
		t.Fatal("Could not query log")
	}

	rowURL := strings.Replace(logRowDetails, "{id}", log.ID.String(), 1)
	req, _ = http.NewRequest("GET", rowURL, nil)
	req.Header.Set(CookieHeader, adminUserCookie)

	res, err = client.Do(req)
	apiutil.ResponseError(t, res, http.StatusOK, err)

	req.Header.Set(CookieHeader, workerUserCookie)
	res, err = client.Do(req)
	apiutil.ResponseError(t, res, http.StatusForbidden, err)
}
