// Package models contains the types for schema 'public'.
package models

// Code generated by xo. DO NOT EDIT.

import (
	"database/sql/driver"
	"errors"
)

// HTTPOperation is the 'http_operation' enum type from schema 'public'.
type HTTPOperation uint16

const (
	// HTTPOperationPost is the 'POST' HTTPOperation.
	HTTPOperationPost = HTTPOperation(1)

	// HTTPOperationPut is the 'PUT' HTTPOperation.
	HTTPOperationPut = HTTPOperation(2)

	// HTTPOperationDelete is the 'DELETE' HTTPOperation.
	HTTPOperationDelete = HTTPOperation(3)
)

// String returns the string value of the HTTPOperation.
func (ho HTTPOperation) String() string {
	var enumVal string

	switch ho {
	case HTTPOperationPost:
		enumVal = "POST"

	case HTTPOperationPut:
		enumVal = "PUT"

	case HTTPOperationDelete:
		enumVal = "DELETE"
	}

	return enumVal
}

// MarshalText marshals HTTPOperation into text.
func (ho HTTPOperation) MarshalText() ([]byte, error) {
	return []byte(ho.String()), nil
}

// UnmarshalText unmarshals HTTPOperation from text.
func (ho *HTTPOperation) UnmarshalText(text []byte) error {
	switch string(text) {
	case "POST":
		*ho = HTTPOperationPost

	case "PUT":
		*ho = HTTPOperationPut

	case "DELETE":
		*ho = HTTPOperationDelete

	default:
		return errors.New("invalid HTTPOperation")
	}

	return nil
}

// Value satisfies the sql/driver.Valuer interface for HTTPOperation.
func (ho HTTPOperation) Value() (driver.Value, error) {
	return ho.String(), nil
}

// Scan satisfies the database/sql.Scanner interface for HTTPOperation.
func (ho *HTTPOperation) Scan(item interface{}) error {
	buf, ok := item.([]byte)
	if !ok {
		return errors.New("invalid HTTPOperation")
	}

	return ho.UnmarshalText(buf)
}