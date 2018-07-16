package config

import (
	"encoding/json"
	"errors"
	"fmt"
	"net/http"
	"sort"
	"strconv"
	"strings"
	"time"

	uuid "github.com/satori/go.uuid"

	"github.com/TravisS25/httputil"
	"github.com/TravisS25/httputil/apiutil"
	"github.com/TravisS25/httputil/formutil"

	"github.com/TravisS25/inventory-tracking/src/server/models"
	_ "github.com/lib/pq"

	"html/template"

	"github.com/TravisS25/httputil/cacheutil"
	"github.com/TravisS25/httputil/confutil"
	"github.com/TravisS25/httputil/dbutil"
	"github.com/TravisS25/httputil/mailutil"

	"github.com/go-redis/redis"
	"github.com/gorilla/csrf"
	"github.com/gorilla/sessions"
	redistore "gopkg.in/boj/redistore.v1"
)

const (
	// LOGFILE is the global file where logs will be written to
	LOGFILE = "/var/log/inventory-tracking.log"
)

var (
	// Conf is the global variable that contains configuration settings read from
	// our config.yaml file
	Conf *confutil.Settings

	// Template is used as global template parser to parse html pages generally for sending emails
	Template *template.Template

	// SessionStore is the global store interface that handles sessions in our app
	SessionStore sessions.Store

	// Cache is the global interface that handles all caching besides sessions
	Cache cacheutil.CacheStore

	// Mailer is global interface for sending mail/message
	Mailer mailutil.SendMessage

	// CSRF is the global func used to implement csrf middleware
	CSRF func(http.Handler) http.Handler

	// FormValidation is global form validator settings for app
	FormValidation formutil.FormValidation

	// DB is global database variable for app
	DB *dbutil.DB

	// RouterPaths is global map for containing api end points for router
	RouterPaths = make(map[string]string)

	// Routing is global map for indicating which routes a user has access
	// to depending on their group
	Routing = make(map[string][]string)
)

func init() {
	initConfigSettings()
	initCacheSettings()
	initDB()
	initStoreSettings()
	initMessenger()
	initTemplate()
	initCSRF()
	initRouterPaths()
	initRouting()
	//initCacheReset()
	initFormValidation()
}

func initFormValidation() {
	FormValidation = formutil.FormValidation{}
	FormValidation.SetQuerier(DB)
	FormValidation.SetCache(Cache)
}

func initMessenger() {
	if Conf.EmailConfig.TestMode {
		Mailer = mailutil.NewMailMessenger(mailutil.MailerConfig{
			Host:     Conf.EmailConfig.TestEmail.Host,
			Port:     Conf.EmailConfig.TestEmail.Port,
			User:     Conf.EmailConfig.TestEmail.User,
			Password: Conf.EmailConfig.TestEmail.Password,
		})
	} else {
		Mailer = mailutil.NewMailMessenger(mailutil.MailerConfig{
			Host:     Conf.EmailConfig.LiveEmail.Host,
			Port:     Conf.EmailConfig.LiveEmail.Port,
			User:     Conf.EmailConfig.LiveEmail.User,
			Password: Conf.EmailConfig.LiveEmail.Password,
		})
	}
}

func initDB() {
	var err error
	DB, err = dbutil.NewDB(dbutil.DBConfig{
		Host:     Conf.DatabaseConfig.Prod.Host,
		User:     Conf.DatabaseConfig.Prod.User,
		Password: Conf.DatabaseConfig.Prod.Password,
		DBName:   Conf.DatabaseConfig.Prod.DBName,
		Port:     Conf.DatabaseConfig.Prod.Port,
		SSLMode:  Conf.DatabaseConfig.Prod.SSlMode,
	})

	if err != nil {
		panic(err)
	}
}

func initTemplate() {
	templateDir := Conf.TemplatesDir
	Template = template.Must(template.ParseGlob(templateDir))
}

func initConfigSettings() {
	Conf = confutil.ConfigSettings("INVENTORY_TRACKING_CONFIG")
}

func initCacheSettings() {
	if Conf.Cache.Redis != nil {
		redisClient := redis.NewClient(&redis.Options{
			Addr:     Conf.Cache.Redis.Address,
			Password: Conf.Cache.Redis.Password,
			DB:       Conf.Cache.Redis.DB,
		})
		Cache = cacheutil.NewClientCache(redisClient)
	}
}

func initStoreSettings() {
	var err error

	if Conf.Store.Redis != nil {
		SessionStore, err = redistore.NewRediStore(
			Conf.Store.Redis.Size,
			Conf.Store.Redis.Network,
			Conf.Store.Redis.Address,
			Conf.Store.Redis.Password,
			[]byte(Conf.Store.Redis.AuthKey),
			[]byte(Conf.Store.Redis.EncryptKey),
		)
	} else if Conf.Store.FileSystemStore != nil {
		SessionStore = sessions.NewFilesystemStore(
			"/tmp",
			[]byte(Conf.Store.FileSystemStore.AuthKey),
			[]byte(Conf.Store.FileSystemStore.EncryptKey),
		)
	} else {
		SessionStore = sessions.NewCookieStore(
			[]byte(Conf.Store.CookieStore.AuthKey),
			[]byte(Conf.Store.CookieStore.EncryptKey),
		)
	}

	if err != nil {
		panic(err)
	}
}

func initCSRF() {
	CSRF = csrf.Protect([]byte(Conf.CSRF), csrf.Secure(false), csrf.CookieName("csrf"))
}

func initRouting() {
	anonURLs := []string{
		RouterPaths["login"],
		RouterPaths["resetPassword"],
		RouterPaths["confirmPasswordReset"],
		RouterPaths["userDetails"],
	}

	userURLs := append(anonURLs, []string{
		RouterPaths["logout"],
		RouterPaths["changePassword"],
		"/api/machine",
	}...)

	adminURLs := append(userURLs, []string{
		"/api/log",
	}...)

	//Routing["Anon"] = anonUrls
	Routing["User"] = userURLs
	Routing["Admin"] = adminURLs
}

func initRouterPaths() {
	// Account Urls
	RouterPaths["login"] = "/api/account/login/"
	RouterPaths["logout"] = "/api/account/logout/"
	RouterPaths["userDetails"] = "/api/account/user/details/"
	RouterPaths["changePassword"] = "/api/account/change-password/"
	RouterPaths["resetPassword"] = "/api/account/reset-password/"
	RouterPaths["confirmPasswordReset"] = "/api/account/confirm-password-reset/{token}/"
	RouterPaths["emailExists"] = "/api/account/email-exists/{email}/"

	// Machine Urls
	RouterPaths["machineUpload"] = "/api/machine/upload/"
	RouterPaths["machineSearch"] = "/api/machine/search/"
	RouterPaths["machineDetails"] = "/api/machine/details/{id:[0-9]+}/"
	RouterPaths["machineSwap"] = "/api/machine/swap/{oldID:[0-9]+}/{newID:[0-9]+}/"
	RouterPaths["machineEdit"] = "/api/machine/edit/{id:[0-9]+}/"

	// Logging Urls
	RouterPaths["logIndex"] = "/api/log/index/"
	RouterPaths["logDetails"] = "/api/log/details/{userID:[0-9]+}/{apiURL}/"
	RouterPaths["logRowDetails"] = "/api/log/row-details/{id}/"
}

func initCacheReset() {
	users, err := models.QueryUserProfiles(DB, "select * from user_profile")

	if err != nil {
		panic(err)
	}

	for _, user := range users {
		userGroupJoinQuery :=
			`
		select 
			user_group.name as "user_group.name"
		from
			user_group_join
		join
			user_group on user_group_join.user_group_id = user_group.id
		where 
			user_group_join.user_profile_id = $1;
		`

		userGroupJoins, err := models.QueryUserGroupJoins(DB, userGroupJoinQuery, user.ID)

		if err != nil {
			panic(err)
		}

		var urlArray []string
		for _, groupJoin := range userGroupJoins {
			if val, ok := Routing[groupJoin.UserGroup.Name]; ok {
				urlArray = append(urlArray, val...)
			}
		}

		sort.Strings(urlArray)
		urlBytes, err := json.Marshal(urlArray)
		key := fmt.Sprintf(confutil.URLKey, user.Email)
		Cache.Set(key, urlBytes, 0)
	}
}

func LogInserter(w http.ResponseWriter, r *http.Request, payload []byte, db httputil.DBInterface) error {
	var userID *int
	var operation models.HTTPOperation
	usePayload := true
	userBytes := apiutil.GetUser(r)
	currentTime := time.Now().UTC().Format(confutil.DateTimeLayout)

	if userBytes != nil {
		var user models.UserProfile
		json.Unmarshal(userBytes, &user)
		userID = &user.ID
	} else {
		if strings.Contains(r.URL.Path, "login") {
			usePayload = false
			id, _ := strconv.Atoi(w.Header().Get("id"))
			w.Header().Del("id")
			userID = &id
		}
	}

	if r.Method == "POST" {
		operation = models.HTTPOperationPost
	} else if r.Method == "PUT" {
		operation = models.HTTPOperationPut
	} else if r.Method == "DELETE" {
		operation = models.HTTPOperationDelete
	}

	id, err := uuid.NewV4()

	if err != nil {
		return err
	}

	tempURL := r.URL.Path[1 : len(r.URL.Path)-1]
	url := strings.Replace(tempURL, "/", "-", -1)
	logger := models.LoggingHistory{
		ID:          id,
		DateEntered: &currentTime,
		APIURL:      url,
		Operation:   operation,
		EnteredByID: userID,
	}

	var i interface{}
	err = json.Unmarshal(payload, &i)
	if err != nil {
		return err
	}

	if payload != nil && usePayload {
		val, ok := i.(map[string]interface{})

		if !ok {
			arr, ok := i.([]interface{})

			if ok {
				newV := make(map[string]interface{})
				newV["array"] = arr
				logger.JSONData = newV
			} else {
				return errors.New("Not valid json")
			}
		} else {
			logger.JSONData = val
		}
	} else {
		fmt.Println("made to null json")
		logger.JSONData = make(map[string]interface{})
	}

	logger.Insert(db)

	return nil
}
