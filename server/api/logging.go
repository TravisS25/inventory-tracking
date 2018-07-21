package api

import (
	"net/http"

	"github.com/gorilla/mux"

	"github.com/TravisS25/httputil/apiutil"

	"github.com/TravisS25/inventory-tracking/src/server/models"

	"github.com/TravisS25/httputil"
	"github.com/TravisS25/httputil/queryutil"
	"github.com/jmoiron/sqlx"
)

type LoggingAPI struct {
	db httputil.DBInterface
}

func NewLoggingAPI(db httputil.DBInterface) *LoggingAPI {
	return &LoggingAPI{
		db: db,
	}
}

func (l *LoggingAPI) Index(w http.ResponseWriter, r *http.Request) {
	var data interface{}
	takeLimit := uint64(100)

	selectStmt := queryutil.Select
	countSelect := queryutil.CountSelect("logging_history.id")
	loggingSelect :=
		`
		logging_history.*
	`
	fromAndWhereClause :=
		`
	from
		logging_history
	`
	fieldNames := []string{
		"entered_by_id",
		"date_entered",
		"operation",
	}

	query := selectStmt + loggingSelect + fromAndWhereClause
	countQuery := selectStmt + countSelect + fromAndWhereClause

	results, count, err := queryutil.GetFilteredResults(
		r,
		&query,
		&countQuery,
		takeLimit,
		sqlx.DOLLAR,
		nil,
		fieldNames,
		l.db,
	)

	if err != nil {
		w.WriteHeader(http.StatusNotAcceptable)
		w.Write([]byte(err.Error()))
		return
	}

	logs := make([]models.LoggingHistory, 0)
	for results.Next() {
		logging := models.LoggingHistory{}
		results.Scan(
			&logging.DateEntered,
			&logging.APIURL,
			&logging.Operation,
			&logging.JSONData,
		)
		logs = append(logs, logging)
	}

	if len(logs) == 0 {
		data = []interface{}{}
	} else {
		data = logs
	}

	apiutil.SendPayload(w, r, map[string]interface{}{
		"data":  data,
		"total": count,
	})
}

func (l *LoggingAPI) RowDetails(w http.ResponseWriter, r *http.Request) {
	id := mux.Vars(r)["id"]

	log, err := models.QueryLoggingHistory(
		l.db,
		"select * from logging_history where id = $1",
		id,
	)

	if apiutil.HasQueryError(w, err, "Log not found") {
		return
	}

	apiutil.SendPayload(w, r, map[string]interface{}{
		"log": log,
	})
}

func (l *LoggingAPI) LogDetails(w http.ResponseWriter, r *http.Request) {
	userID := mux.Vars(r)["userID"]
	apiURL := mux.Vars(r)["apiURL"]

	logs, err := models.QueryLoggingHistories(
		l.db,
		`
		select 
			* 
		from 
			logging_history 
		where 
			entered_by_id = $1
		and
			api_url = $2;
		`,
		userID,
		apiURL,
	)

	if err != nil {
		apiutil.ServerError(w, err, "")
		return
	}

	apiutil.SendPayload(w, r, map[string]interface{}{
		"logs": logs,
	})
}
