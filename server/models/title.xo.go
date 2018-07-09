// Package models contains the types for schema 'public'.
package models

// Code generated by xo. DO NOT EDIT.

import (
	"github.com/TravisS25/httputil"
)

// Title represents a row from 'public.title'.
type Title struct {
	ID          int    `json:"id,omitempty" db:"id"`                    // id
	Title       string `json:"title,omitempty" db:"title"`              // title
	DateCreated string `json:"dateCreated,omitempty" db:"date_created"` // date_created
}

func QueryTitle(db httputil.SqlxDB, query string, args ...interface{}) (*Title, error) {
	var dest Title
	err := db.Get(&dest, query, args...)
	return &dest, err
}

func QueryTitles(db httputil.SqlxDB, query string, args ...interface{}) ([]*Title, error) {
	var dest []*Title
	err := db.Select(&dest, query, args...)
	return dest, err
}

// Insert inserts the Title to the database.
func (t *Title) Insert(db httputil.XODB) error {
	var err error

	// sql insert query, primary key provided by sequence
	const sqlstr = `INSERT INTO public.title (` +
		`title, date_created` +
		`) VALUES (` +
		`$1, $2` +
		`) RETURNING id`

	// run query
	XOLog(sqlstr, t.Title, t.DateCreated)
	err = db.QueryRow(sqlstr, t.Title, t.DateCreated).Scan(&t.ID)
	if err != nil {
		return err
	}

	return nil
}

// Update updates the Title in the database.
func (t *Title) Update(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `UPDATE public.title SET (` +
		`title, date_created` +
		`) = ( ` +
		`$1, $2` +
		`) WHERE id = $3`

	// run query
	XOLog(sqlstr, t.Title, t.DateCreated, t.ID)
	_, err = db.Exec(sqlstr, t.Title, t.DateCreated, t.ID)
	return err
}

// Upsert performs an upsert for Title.
//
// NOTE: PostgreSQL 9.5+ only
func (t *Title) Upsert(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `INSERT INTO public.title (` +
		`id, title, date_created` +
		`) VALUES (` +
		`$1, $2, $3` +
		`) ON CONFLICT (id) DO UPDATE SET (` +
		`id, title, date_created` +
		`) = (` +
		`EXCLUDED.id, EXCLUDED.title, EXCLUDED.date_created` +
		`)`

	// run query
	XOLog(sqlstr, t.ID, t.Title, t.DateCreated)
	_, err = db.Exec(sqlstr, t.ID, t.Title, t.DateCreated)
	if err != nil {
		return err
	}

	return nil
}

// Delete deletes the Title from the database.
func (t *Title) Delete(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `DELETE FROM public.title WHERE id = $1`

	// run query
	XOLog(sqlstr, t.ID)
	_, err = db.Exec(sqlstr, t.ID)
	if err != nil {
		return err
	}

	return nil
}