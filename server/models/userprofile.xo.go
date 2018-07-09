// Package models contains the types for schema 'public'.
package models

// Code generated by xo. DO NOT EDIT.

import (
	"github.com/TravisS25/httputil"
)

// UserProfile represents a row from 'public.user_profile'.
type UserProfile struct {
	ID         int    `json:"id,omitempty" db:"id"`                  // id
	TitleID    int    `json:"titleID,omitempty" db:"title_id"`       // title_id
	Email      string `json:"email,omitempty" db:"email"`            // email
	Password   string `json:"-" db:"password"`                       // password
	FirstName  string `json:"firstName,omitempty" db:"first_name"`   // first_name
	LastName   string `json:"lastName,omitempty" db:"last_name"`     // last_name
	IsActive   bool   `json:"isActive" db:"is_active"`               // is_active
	DateJoined string `json:"dateJoined,omitempty" db:"date_joined"` // date_joined
	Title      *Title `json:"title,omitempty" db:"title"`
}

func QueryUserProfile(db httputil.SqlxDB, query string, args ...interface{}) (*UserProfile, error) {
	var dest UserProfile
	err := db.Get(&dest, query, args...)
	return &dest, err
}

func QueryUserProfiles(db httputil.SqlxDB, query string, args ...interface{}) ([]*UserProfile, error) {
	var dest []*UserProfile
	err := db.Select(&dest, query, args...)
	return dest, err
}

// Insert inserts the UserProfile to the database.
func (up *UserProfile) Insert(db httputil.XODB) error {
	var err error

	// sql insert query, primary key provided by sequence
	const sqlstr = `INSERT INTO public.user_profile (` +
		`title_id, email, password, first_name, last_name, is_active, date_joined` +
		`) VALUES (` +
		`$1, $2, $3, $4, $5, $6, $7` +
		`) RETURNING id`

	// run query
	XOLog(sqlstr, up.TitleID, up.Email, up.Password, up.FirstName, up.LastName, up.IsActive, up.DateJoined)
	err = db.QueryRow(sqlstr, up.TitleID, up.Email, up.Password, up.FirstName, up.LastName, up.IsActive, up.DateJoined).Scan(&up.ID)
	if err != nil {
		return err
	}

	return nil
}

// Update updates the UserProfile in the database.
func (up *UserProfile) Update(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `UPDATE public.user_profile SET (` +
		`title_id, email, password, first_name, last_name, is_active, date_joined` +
		`) = ( ` +
		`$1, $2, $3, $4, $5, $6, $7` +
		`) WHERE id = $8`

	// run query
	XOLog(sqlstr, up.TitleID, up.Email, up.Password, up.FirstName, up.LastName, up.IsActive, up.DateJoined, up.ID)
	_, err = db.Exec(sqlstr, up.TitleID, up.Email, up.Password, up.FirstName, up.LastName, up.IsActive, up.DateJoined, up.ID)
	return err
}

// Upsert performs an upsert for UserProfile.
//
// NOTE: PostgreSQL 9.5+ only
func (up *UserProfile) Upsert(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `INSERT INTO public.user_profile (` +
		`id, title_id, email, password, first_name, last_name, is_active, date_joined` +
		`) VALUES (` +
		`$1, $2, $3, $4, $5, $6, $7, $8` +
		`) ON CONFLICT (id) DO UPDATE SET (` +
		`id, title_id, email, password, first_name, last_name, is_active, date_joined` +
		`) = (` +
		`EXCLUDED.id, EXCLUDED.title_id, EXCLUDED.email, EXCLUDED.password, EXCLUDED.first_name, EXCLUDED.last_name, EXCLUDED.is_active, EXCLUDED.date_joined` +
		`)`

	// run query
	XOLog(sqlstr, up.ID, up.TitleID, up.Email, up.Password, up.FirstName, up.LastName, up.IsActive, up.DateJoined)
	_, err = db.Exec(sqlstr, up.ID, up.TitleID, up.Email, up.Password, up.FirstName, up.LastName, up.IsActive, up.DateJoined)
	if err != nil {
		return err
	}

	return nil
}

// Delete deletes the UserProfile from the database.
func (up *UserProfile) Delete(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `DELETE FROM public.user_profile WHERE id = $1`

	// run query
	XOLog(sqlstr, up.ID)
	_, err = db.Exec(sqlstr, up.ID)
	if err != nil {
		return err
	}

	return nil
}