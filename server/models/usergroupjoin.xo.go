// Package models contains the types for schema 'public'.
package models

// Code generated by xo. DO NOT EDIT.

import (
	"github.com/TravisS25/httputil"
)

// UserGroupJoin represents a row from 'public.user_group_join'.
type UserGroupJoin struct {
	ID            int          `json:"id,omitempty" db:"id"`                         // id
	UserProfileID int          `json:"userProfileID,omitempty" db:"user_profile_id"` // user_profile_id
	UserGroupID   int          `json:"userGroupID,omitempty" db:"user_group_id"`     // user_group_id
	UserGroup     *UserGroup   `json:"userGroup,omitempty" db:"user_group"`
	UserProfile   *UserProfile `json:"userProfile,omitempty" db:"user_profile"`
}

func QueryUserGroupJoin(db httputil.SqlxDB, query string, args ...interface{}) (*UserGroupJoin, error) {
	var dest UserGroupJoin
	err := db.Get(&dest, query, args...)
	return &dest, err
}

func QueryUserGroupJoins(db httputil.SqlxDB, query string, args ...interface{}) ([]*UserGroupJoin, error) {
	var dest []*UserGroupJoin
	err := db.Select(&dest, query, args...)
	return dest, err
}

// Insert inserts the UserGroupJoin to the database.
func (ugj *UserGroupJoin) Insert(db httputil.XODB) error {
	var err error

	// sql insert query, primary key provided by sequence
	const sqlstr = `INSERT INTO public.user_group_join (` +
		`user_profile_id, user_group_id` +
		`) VALUES (` +
		`$1, $2` +
		`) RETURNING id`

	// run query
	XOLog(sqlstr, ugj.UserProfileID, ugj.UserGroupID)
	err = db.QueryRow(sqlstr, ugj.UserProfileID, ugj.UserGroupID).Scan(&ugj.ID)
	if err != nil {
		return err
	}

	return nil
}

// Update updates the UserGroupJoin in the database.
func (ugj *UserGroupJoin) Update(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `UPDATE public.user_group_join SET (` +
		`user_profile_id, user_group_id` +
		`) = ( ` +
		`$1, $2` +
		`) WHERE id = $3`

	// run query
	XOLog(sqlstr, ugj.UserProfileID, ugj.UserGroupID, ugj.ID)
	_, err = db.Exec(sqlstr, ugj.UserProfileID, ugj.UserGroupID, ugj.ID)
	return err
}

// Upsert performs an upsert for UserGroupJoin.
//
// NOTE: PostgreSQL 9.5+ only
func (ugj *UserGroupJoin) Upsert(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `INSERT INTO public.user_group_join (` +
		`id, user_profile_id, user_group_id` +
		`) VALUES (` +
		`$1, $2, $3` +
		`) ON CONFLICT (id) DO UPDATE SET (` +
		`id, user_profile_id, user_group_id` +
		`) = (` +
		`EXCLUDED.id, EXCLUDED.user_profile_id, EXCLUDED.user_group_id` +
		`)`

	// run query
	XOLog(sqlstr, ugj.ID, ugj.UserProfileID, ugj.UserGroupID)
	_, err = db.Exec(sqlstr, ugj.ID, ugj.UserProfileID, ugj.UserGroupID)
	if err != nil {
		return err
	}

	return nil
}

// Delete deletes the UserGroupJoin from the database.
func (ugj *UserGroupJoin) Delete(db httputil.XODB) error {
	var err error

	// sql query
	const sqlstr = `DELETE FROM public.user_group_join WHERE id = $1`

	// run query
	XOLog(sqlstr, ugj.ID)
	_, err = db.Exec(sqlstr, ugj.ID)
	if err != nil {
		return err
	}

	return nil
}
