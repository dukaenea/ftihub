--Create table t_users
CREATE TABLE `ftihub`.`t_users` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`));
--Create table t_privatechat
CREATE TABLE `ftihub`.`t_privatechat` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `id_sender` INT(11) NOT NULL,
  `id_receiver` INT(11) NOT NULL,
  `online` TINYINT(4) NOT NULL,
  `message` LONGTEXT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_sender_idx` (`id_sender` ASC),
  INDEX `fk_receiver_idx` (`id_receiver` ASC),
  CONSTRAINT `fk_sender`
    FOREIGN KEY (`id_sender`)
    REFERENCES `ftihub`.`t_users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_receiver`
    FOREIGN KEY (`id_receiver`)
    REFERENCES `ftihub`.`t_users` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
