package api

import (
	"encoding/json"

	"github.com/TravisS25/inventory-tracking/src/server/models"
)

const (
	LOGFILE = "/var/log/"
)

func GetUser(userBytes []byte) (models.UserProfile, error) {
	var user models.UserProfile
	err := json.Unmarshal(userBytes, &user)
	return user, err
}
