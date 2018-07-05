// Package models contains the types for schema 'public'.
package models

// Code generated by xo. DO NOT EDIT.

import (
	"github.com/TravisS25/httputil"
)

// Room represents a row from 'public.room'.
type Room struct {
	ID           int         `json:"id,omitempty" db:"id"`                      // id
	DepartmentID int         `json:"departmentID,omitempty" db:"department_id"` // department_id
	Name         string      `json:"name,omitempty" db:"name"`                  // name
	Department   *Department `json:"department,omitempty" db:"department"`
}

func QueryRoom(db httputil.SqlxDB, query string, args ...interface{}) (*Room, error) {
	var dest Room
	err := db.Get(&dest, query, args...)
	return &dest, err
}

func QueryRooms(db httputil.SqlxDB, query string, args ...interface{}) ([]*Room, error) {
	var dest []*Room
	err := db.Select(&dest, query, args...)
	return dest, err
}

// Insert inserts the Room to the database.
func (r *Room) Insert(db httputil.XODB) error {
	var err error

	// sql insert query, primary key provided by sequence
	const sqlstr = `INSERT INTO public.room (` +
		`department_id, name` +
		`) VALUES (` +
		`$1, $2` +
		`) RETURNING id`

	// run query
	XOLog(sqlstr, r.DepartmentID, r.Name)
	err = db.QueryRow(sqlstr, r.DepartmentID, r.Name).Scan(&r.ID)
	if err != nil {
		return err
	}

	return nil
}

// Update updates the Room in the database.
func (r *Room) Update(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `UPDATE public.room SET (` +
		`department_id, name` +
		`) = ( ` +
		`$1, $2` +
		`) WHERE id = $3`

	// run query
	XOLog(sqlstr, r.DepartmentID, r.Name, r.ID)
	_, err = db.Exec(sqlstr, r.DepartmentID, r.Name, r.ID)
	return err
}

// Upsert performs an upsert for Room.
//
// NOTE: PostgreSQL 9.5+ only
func (r *Room) Upsert(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `INSERT INTO public.room (` +
		`id, department_id, name` +
		`) VALUES (` +
		`$1, $2, $3` +
		`) ON CONFLICT (id) DO UPDATE SET (` +
		`id, department_id, name` +
		`) = (` +
		`EXCLUDED.id, EXCLUDED.department_id, EXCLUDED.name` +
		`)`

	// run query
	XOLog(sqlstr, r.ID, r.DepartmentID, r.Name)
	_, err = db.Exec(sqlstr, r.ID, r.DepartmentID, r.Name)
	if err != nil {
		return err
	}

	return nil
}

// Delete deletes the Room from the database.
func (r *Room) Delete(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `DELETE FROM public.room WHERE id = $1`

	// run query
	XOLog(sqlstr, r.ID)
	_, err = db.Exec(sqlstr, r.ID)
	if err != nil {
		return err
	}

	return nil
}
