CREATE TABLE user (
	id INT NOT NULL AUTO_INCREMENT,
	username LONGTEXT NOT NULL,
	PRIMARY KEY (id)
) ENGINE=InnoDB;

INSERT INTO user (username) VALUES ("root");
INSERT INTO user (username) VALUES ("admin");
INSERT INTO user (username) VALUES ("user");