// Package models contains the types for schema 'public'.
package models

// Code generated by xo. DO NOT EDIT.

import (
	"github.com/TravisS25/httputil"
)

// Machine represents a row from 'public.machine'.
type Machine struct {
	ID              int            `json:"id,omitempty" db:"id"`                             // id
	RoomID          int            `json:"roomID,omitempty" db:"room_id"`                    // room_id
	MachineStatusID int            `json:"machineStatusID,omitempty" db:"machine_status_id"` // machine_status_id
	MachineName     string         `json:"machineName,omitempty" db:"machine_name"`          // machine_name
	ScannedTime     string         `json:"scannedTime,omitempty" db:"scanned_time"`          // scanned_time
	MachineStatus   *MachineStatus `json:"machineStatus,omitempty" db:"machine_status"`
	Room            *Room          `json:"room,omitempty" db:"room"`
}

func QueryMachine(db httputil.SqlxDB, query string, args ...interface{}) (*Machine, error) {
	var dest Machine
	err := db.Get(&dest, query, args...)
	return &dest, err
}

func QueryMachines(db httputil.SqlxDB, query string, args ...interface{}) ([]*Machine, error) {
	var dest []*Machine
	err := db.Select(&dest, query, args...)
	return dest, err
}

// Insert inserts the Machine to the database.
func (m *Machine) Insert(db httputil.XODB) error {
	var err error

	// sql insert query, primary key provided by sequence
	const sqlstr = `INSERT INTO public.machine (` +
		`room_id, machine_status_id, machine_name, scanned_time` +
		`) VALUES (` +
		`$1, $2, $3, $4` +
		`) RETURNING id`

	// run query
	XOLog(sqlstr, m.RoomID, m.MachineStatusID, m.MachineName, m.ScannedTime)
	err = db.QueryRow(sqlstr, m.RoomID, m.MachineStatusID, m.MachineName, m.ScannedTime).Scan(&m.ID)
	if err != nil {
		return err
	}

	return nil
}

// Update updates the Machine in the database.
func (m *Machine) Update(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `UPDATE public.machine SET (` +
		`room_id, machine_status_id, machine_name, scanned_time` +
		`) = ( ` +
		`$1, $2, $3, $4` +
		`) WHERE id = $5`

	// run query
	XOLog(sqlstr, m.RoomID, m.MachineStatusID, m.MachineName, m.ScannedTime, m.ID)
	_, err = db.Exec(sqlstr, m.RoomID, m.MachineStatusID, m.MachineName, m.ScannedTime, m.ID)
	return err
}

// Upsert performs an upsert for Machine.
//
// NOTE: PostgreSQL 9.5+ only
func (m *Machine) Upsert(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `INSERT INTO public.machine (` +
		`id, room_id, machine_status_id, machine_name, scanned_time` +
		`) VALUES (` +
		`$1, $2, $3, $4, $5` +
		`) ON CONFLICT (id) DO UPDATE SET (` +
		`id, room_id, machine_status_id, machine_name, scanned_time` +
		`) = (` +
		`EXCLUDED.id, EXCLUDED.room_id, EXCLUDED.machine_status_id, EXCLUDED.machine_name, EXCLUDED.scanned_time` +
		`)`

	// run query
	XOLog(sqlstr, m.ID, m.RoomID, m.MachineStatusID, m.MachineName, m.ScannedTime)
	_, err = db.Exec(sqlstr, m.ID, m.RoomID, m.MachineStatusID, m.MachineName, m.ScannedTime)
	if err != nil {
		return err
	}

	return nil
}

// Delete deletes the Machine from the database.
func (m *Machine) Delete(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `DELETE FROM public.machine WHERE id = $1`

	// run query
	XOLog(sqlstr, m.ID)
	_, err = db.Exec(sqlstr, m.ID)
	if err != nil {
		return err
	}

	return nil
}