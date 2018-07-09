package forms

import (
	"github.com/TravisS25/httputil/dbutil"
	"github.com/TravisS25/httputil/formutil"
	"github.com/TravisS25/inventory-tracking/src/server/config"
)

var (
	TestFormValidation = formutil.FormValidation{}
	TestDB             *dbutil.DB
)

func init() {
	initDB()
	initFormValidation()
}

func initDB() {
	var err error
	TestDB, err = dbutil.NewDB(dbutil.DBConfig{
		Host:     config.Conf.DatabaseConfig.Test.Host,
		User:     config.Conf.DatabaseConfig.Test.User,
		Password: config.Conf.DatabaseConfig.Test.Password,
		DBName:   config.Conf.DatabaseConfig.Test.DBName,
		Port:     config.Conf.DatabaseConfig.Test.Port,
		SSLMode:  config.Conf.DatabaseConfig.Test.SSlMode,
	})

	if err != nil {
		panic(err)
	}
}

func initFormValidation() {
	TestFormValidation.SetQuerier(TestDB)
	TestFormValidation.SetCache(config.Cache)
}
