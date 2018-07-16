package api

// func TestLoggingAPIs(t *testing.T) {
// 	var req *http.Request
// 	var res *http.Response
// 	var err error
// 	var buffer bytes.Buffer
// 	var token, csrfCookie, userCookie string

// 	ts := httptest.NewServer(App())
// 	defer ts.Close()
// 	userCookie, err = loginUser(AdminEmail, TestPassword, ts)

// 	if err != nil {
// 		t.Fatal("Could not login user")
// 	}

// 	client := &http.Client{}
// 	baseURL := ts.URL
// 	logIndexURL := baseURL + config.RouterPaths["logIndex"] + "?take=20&skip=0"
// 	logDetailsURL := baseURL + config.RouterPaths["logDetails"]
// 	logRowDetails := baseURL + config.RouterPaths["logRowDetails"]

// 	// -----------------------------------------------------------------
// 	//
// 	// Log Index API

// 	req, err = http.NewRequest("GET", logIndexURL, nil)
// 	req.Header.Set(CookieHeader, userCookie)

// 	if err != nil {
// 		t.Fatal("err on request")
// 	}
// }
