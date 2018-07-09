package api

import (
	"encoding/json"
	"fmt"
	"net/http"
	"time"

	"github.com/TravisS25/httputil/confutil"

	"github.com/TravisS25/httputil/cacheutil"

	"github.com/gorilla/mux"

	"github.com/jmoiron/sqlx"

	"github.com/TravisS25/httputil/queryutil"
	"github.com/go-ozzo/ozzo-validation"

	"github.com/TravisS25/httputil/apiutil"
	"github.com/TravisS25/httputil/dbutil"
	"github.com/TravisS25/httputil/formutil"
	"github.com/TravisS25/inventory-tracking/src/server/forms"
	"github.com/TravisS25/inventory-tracking/src/server/models"

	"github.com/TravisS25/httputil"
)

type MachineAPI struct {
	db      httputil.DBInterface
	cache   cacheutil.CacheStore
	formMap map[string]formutil.Validator
}

func NewMachineAPI(db httputil.DBInterface, cache cacheutil.CacheStore, formMap map[string]formutil.Validator) *MachineAPI {
	return &MachineAPI{
		db:      db,
		cache:   cache,
		formMap: formMap,
	}
}

func (m *MachineAPI) MachineUpload(w http.ResponseWriter, r *http.Request) {
	if r.Method == "GET" {
		//helpers.SendPayload(w, helpers.GetCSRFPayload(w, r))
	} else if r.Method == "POST" {
		if apiutil.HasBodyError(w, r) {
			return
		}

		var forms []forms.MachineForm
		dec := json.NewDecoder(r.Body)
		err := dec.Decode(&forms)

		if apiutil.HasDecodeError(w, err) {
			return
		}

		if len(forms) == 0 {
			w.WriteHeader(http.StatusNotAcceptable)
			w.Write([]byte("Can't have empty list"))
			return
		}

		tx, _ := m.db.Begin()

		formErrors := make([]validation.Errors, 0)
		for _, form := range forms {
			fmt.Println(form.MachineName)
			err = m.formMap["form"].Validate(form)

			if err == nil {
				machine := models.Machine{
					RoomID:          form.RoomID,
					MachineStatusID: form.MachineStatusID,
					MachineName:     form.MachineName,
					ScannedTime:     time.Now().UTC().Format(confutil.DateTimeLayout),
				}

				err = machine.Insert(tx)

				if err != nil {
					apiutil.ServerError(w, err, "")
					tx.Rollback()
					return
				}
			} else {
				formErrors = append(formErrors, err.(validation.Errors))
			}
		}

		if len(formErrors) == 0 {
			err = tx.Commit()

			if err != nil {
				apiutil.ServerError(w, err, "")
				return
			}
		} else {
			tx.Rollback()
			stringify, err := json.Marshal(formErrors)
			w.WriteHeader(http.StatusNotAcceptable)
			w.Write(stringify)

			if err != nil {
				apiutil.ServerError(w, err, "")
				return
			}
		}
	}
}

func (m *MachineAPI) MachineSearch(w http.ResponseWriter, r *http.Request) {
	var machines []*models.Machine
	var data interface{}
	var err error
	var query string
	var countQuery string
	takeLimit := uint64(100)
	selectStmt := queryutil.Select
	countSelect := queryutil.CountSelect("machine.id")
	machineSelect :=
		`
	machine.machine_name,
	machine.scanned_time,
	room.name as "room.name",
	department.name as "room.department.name",
	building_floor.name as "room.department.building_floor.name",
	building.name as "room.department.building_floor.building.name"
	`
	fromClause :=
		`
	from
		machine
	join
		room on machine.room_id = room.id
	join
		department on room.department_id = department.id
	join
		building_floor on department.building_floor_id = building_floor.id
	join
		building on building_floor.building_id = building.id
	`

	fieldNames := []string{
		"machine_name",
		"scanned_time",
		"room.name",
		"department.name",
		"building_floor.name",
		"building.name",
	}

	query = selectStmt + machineSelect + fromClause
	countQuery = selectStmt + countSelect + fromClause

	replacements, err := queryutil.ApplyAll(
		r,
		&query,
		takeLimit,
		sqlx.DOLLAR,
		nil,
		fieldNames,
	)

	if err != nil {
		w.WriteHeader(http.StatusNotAcceptable)
		w.Write([]byte(err.Error()))
		return
	}

	machines, err = models.QueryMachines(
		m.db,
		query,
		replacements...,
	)

	countReplacements, err := queryutil.WhereFilter(
		r,
		&countQuery,
		sqlx.DOLLAR,
		nil,
		fieldNames,
	)

	if err != nil {
		w.WriteHeader(http.StatusNotAcceptable)
		w.Write([]byte(err.Error()))
		return
	}

	count, err := dbutil.QueryCount(
		m.db,
		countQuery,
		countReplacements...,
	)

	if err != nil {
		w.WriteHeader(http.StatusNotAcceptable)
		w.Write([]byte(err.Error()))
		return
	}

	if len(machines) == 0 {
		data = []interface{}{}
	} else {
		data = machines
	}

	apiutil.SendPayload(w, r, false, map[string]interface{}{
		"data":  data,
		"total": count.Total,
	})
}

func (m *MachineAPI) MachineDetails(w http.ResponseWriter, r *http.Request) {
	id := mux.Vars(r)["id"]
	machine, err := models.QueryMachine(
		m.db,
		`
		select
			machine.machine_name,
			machine.scanned_time,
			room.name as "room.name",
			department.name as "room.department.name",
			building_floor.name as "room.department.building_floor.name",
			building.name as "room.department.building_floor.building.name"
		from
			machine
		join
			room on machine.room_id = room.id
		join
			department on room.department_id = department.id
		join
			building_floor on department.building_floor_id = building_floor.id
		join
			building on building_floor.building_id = building.id
		where
			machine.id = $1;
		`,
		id,
	)

	if apiutil.HasQueryError(w, err, "Machine not found") {
		return
	}

	apiutil.SendPayload(w, r, false, map[string]interface{}{
		"machine": machine,
	})

}

func (m *MachineAPI) MachineSwap(w http.ResponseWriter, r *http.Request) {
	if r.Method == "GET" {

	} else if r.Method == "PUT" {
		oldID := mux.Vars(r)["oldID"]
		newID := mux.Vars(r)["newID"]
		query := "select * from machine where id = $1;"

		oldMachine, err := models.QueryMachine(m.db, query, oldID)

		if apiutil.HasQueryError(w, err, "Old machine not found") {
			return
		}

		newMachine, err := models.QueryMachine(m.db, query, newID)

		if apiutil.HasQueryError(w, err, "New machine not found") {
			return
		}

		// In Service
		if oldMachine.MachineStatusID != 1 {
			w.WriteHeader(http.StatusNotAcceptable)
			w.Write([]byte("Machine you are going to swap for has to be in service"))
			return
		}

		var form forms.MachineSwapForm
		dec := json.NewDecoder(r.Body)
		err = dec.Decode(&form)

		if apiutil.HasDecodeError(w, err) {
			return
		}

		err = m.formMap["formSwap"].Validate(form)

		if apiutil.HasFormErrors(w, r, err) {
			return
		}

		newMachine.RoomID = oldMachine.RoomID
		err = newMachine.Update(m.db)

		if err != nil {
			apiutil.ServerError(w, err, "")
			return
		}

		oldMachine.MachineStatusID = form.MachineStatusID
		err = oldMachine.Update(m.db)

		if err != nil {
			apiutil.ServerError(w, err, "")
			return
		}
	}
}

func (m *MachineAPI) MachineEdit(w http.ResponseWriter, r *http.Request) {
	if r.Method == "GET" {

	} else if r.Method == "PUT" {
		id := mux.Vars(r)["id"]
		machine, err := models.QueryMachine(
			m.db,
			"select * from machine where id = $1;",
			id,
		)

		if apiutil.HasQueryError(w, err, "Machine not found") {
			return
		}

		if apiutil.HasBodyError(w, r) {
			return
		}

		var form forms.MachineForm
		decoder := json.NewDecoder(r.Body)
		err = decoder.Decode(&form)

		if apiutil.HasDecodeError(w, err) {
			return
		}

		form.Instance = machine
		err = m.formMap["form"].Validate(form)

		if apiutil.HasFormErrors(w, r, err) {
			return
		}

		machine.MachineName = form.MachineName
		machine.RoomID = form.RoomID
		machine.MachineStatusID = form.MachineStatusID

		err = machine.Update(m.db)

		if err != nil {
			apiutil.ServerError(w, err, "")
			return
		}
	}
}
