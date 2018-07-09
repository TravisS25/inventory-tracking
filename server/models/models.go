package models

import (
	"net/http"
	"strconv"
	"time"

	"github.com/TravisS25/httputil"
	"github.com/TravisS25/httputil/apiutil"
	"github.com/TravisS25/httputil/confutil"
)

// GetEmail returns UserProfile Email field
func (u *UserProfile) GetEmail() string {
	return u.Email
}

// GetID returns UserProfile ID field
func (u *UserProfile) GetID() string {
	return strconv.Itoa(u.ID)
}

type LogEntry struct{}

func (l LogEntry) InsertLog(r *http.Request, payload interface{}, db httputil.DBInterface) error {
	var userID *int
	user := apiutil.GetUser(r)
	currentTime := time.Now().UTC().Format(confutil.DateTimeLayout)

	if user != nil {
		id, _ := strconv.Atoi(user.(apiutil.IUser).GetID())
		userID = &id
	}

	logger := LoggingHistory{
		DateEntered: &currentTime,
		URL:         r.URL.Path,
		Operation:   r.Method,
		EnteredByID: userID,
	}

	if payload != nil {
		payloadS := payload.(string)
		logger.Value = &payloadS
	}

	return logger.Insert(db)
}
