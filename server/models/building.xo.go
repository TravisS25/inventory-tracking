// Package models contains the types for schema 'public'.
package models

// Code generated by xo. DO NOT EDIT.

import (
	"github.com/TravisS25/httputil"
)

// Building represents a row from 'public.building'.
type Building struct {
	ID   int    `json:"id,omitempty" db:"id"`     // id
	Name string `json:"name,omitempty" db:"name"` // name
}

func QueryBuilding(db httputil.SqlxDB, query string, args ...interface{}) (*Building, error) {
	var dest Building
	err := db.Get(&dest, query, args...)
	return &dest, err
}

func QueryBuildings(db httputil.SqlxDB, query string, args ...interface{}) ([]*Building, error) {
	var dest []*Building
	err := db.Select(&dest, query, args...)
	return dest, err
}

// Insert inserts the Building to the database.
func (b *Building) Insert(db httputil.XODB) error {
	var err error

	// sql insert query, primary key provided by sequence
	const sqlstr = `INSERT INTO public.building (` +
		`name` +
		`) VALUES (` +
		`$1` +
		`) RETURNING id`

	// run query
	XOLog(sqlstr, b.Name)
	err = db.QueryRow(sqlstr, b.Name).Scan(&b.ID)
	if err != nil {
		return err
	}

	return nil
}

// Update updates the Building in the database.
func (b *Building) Update(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `UPDATE public.building SET (` +
		`name` +
		`) = ( ` +
		`$1` +
		`) WHERE id = $2`

	// run query
	XOLog(sqlstr, b.Name, b.ID)
	_, err = db.Exec(sqlstr, b.Name, b.ID)
	return err
}

// Upsert performs an upsert for Building.
//
// NOTE: PostgreSQL 9.5+ only
func (b *Building) Upsert(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `INSERT INTO public.building (` +
		`id, name` +
		`) VALUES (` +
		`$1, $2` +
		`) ON CONFLICT (id) DO UPDATE SET (` +
		`id, name` +
		`) = (` +
		`EXCLUDED.id, EXCLUDED.name` +
		`)`

	// run query
	XOLog(sqlstr, b.ID, b.Name)
	_, err = db.Exec(sqlstr, b.ID, b.Name)
	if err != nil {
		return err
	}

	return nil
}

// Delete deletes the Building from the database.
func (b *Building) Delete(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `DELETE FROM public.building WHERE id = $1`

	// run query
	XOLog(sqlstr, b.ID)
	_, err = db.Exec(sqlstr, b.ID)
	if err != nil {
		return err
	}

	return nil
}
