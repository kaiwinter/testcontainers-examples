CREATE TABLE user (
	id INT NOT NULL AUTO_INCREMENT,
	username LONGTEXT NOT NULL,
	login_count INTEGER NOT NULL DEFAULT 0,
	PRIMARY KEY (id)
) ENGINE=InnoDB;

INSERT INTO user (username, login_count) VALUES ("root", 5);
INSERT INTO user (username, login_count) VALUES ("admin", 3);
INSERT INTO user (username, login_count) VALUES ("user", 1);