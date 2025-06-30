
CREATE TABLE `sso_user` (
	`user_id`	        VARCHAR(256)	NOT NULL,
    `email`             VARCHAR(256)    NULL,
    `nickname`	        VARCHAR(256)    NULL,
	`profile`	        VARCHAR(256)    NULL,
	`refresh_token`	    VARCHAR(256)	NULL,
    `token_create_date`	DATETIME	    NULL,
	`modify_date`	    DATETIME	    NULL,
    `create_date`	    DATETIME	    NULL
);

ALTER TABLE `SSO_USER` ADD CONSTRAINT `PK_SSO_USER` PRIMARY KEY (
	`user_id`
);