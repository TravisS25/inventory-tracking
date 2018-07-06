package config

import (
	"encoding/json"
	"fmt"
	"net/http"
	"sort"

	"github.com/TravisS25/httputil/formutil"

	"bitbucket.org/TravisS25/contractor-tracking/contractor-tracking/contractor-server/models"
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

	FormValidation formutil.FormValidation

	// DB is global database variable for app
	DB *dbutil.DB

	// Router is global router
	RouterPaths = make(map[string]string)

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
	initRouting()
	initCacheReset()
	initFormValidation()
	initRouterPaths()
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
	CSRF = csrf.Protect([]byte(Conf.CSRF), csrf.Secure(Conf.HTTPS), csrf.CookieName("csrf"))
}

func initRouting() {
	anonUrls := []string{
		"/api/account/user/details",
		"/api/account/login",
		"/api/account/confirm-password-reset",
		"/api/account/reset-password",
		"/api/account/resend-verification-email",
		"/api/account/logged-in",
	}

	employeeUrls := append(anonUrls, []string{
		"/api/account/logout",
		"/api/contractor",
		"/api/note",
		"/api/found-from",
	}...)

	managerUrls := append(employeeUrls, []string{
		"/api/user",
	}...)

	adminUrls := append(managerUrls, []string{
		"/api/call-status",
		"/api/rejection-reason",
		"/api/areas/all",
	}...)

	Routing["Anon"] = anonUrls
	Routing["Employee"] = employeeUrls
	Routing["Manager"] = managerUrls
	Routing["Admin"] = adminUrls
	Routing["Master"] = adminUrls
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
