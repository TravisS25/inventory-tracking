package models

import (
	"strconv"
)

// GetEmail returns UserProfile Email field
func (u *UserProfile) GetEmail() string {
	return u.Email
}

// GetID returns UserProfile ID field
func (u *UserProfile) GetID() string {
	return strconv.Itoa(u.ID)
}

// type LogEntry struct{}

// func (l LogEntry) InsertLog(r *http.Request, payload []byte, db httputil.DBInterface) error {
// 	var userID *int
// 	userBytes := apiutil.GetUser(r)
// 	currentTime := time.Now().UTC().Format(confutil.DateTimeLayout)

// 	if userBytes != nil {
// 		var user UserProfile
// 		json.Unmarshal(userBytes, &user)
// 		userID = &user.ID
// 	}

// 	logger := LoggingHistory{
// 		DateEntered: &currentTime,
// 		URL:         r.URL.Path,
// 		Operation:   r.Method,
// 		EnteredByID: userID,
// 	}

// 	if payload != "" {
// 		logger.Value = &payload
// 	}

// 	return logger.Insert(db)
// }
