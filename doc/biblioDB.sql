-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema anhalytics_biblio
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema anhalytics_biblio
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `anhalytics_biblio` DEFAULT CHARACTER SET utf8 ;
USE `anhalytics_biblio` ;

-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`COUNTRY`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`COUNTRY` (
  `countryID` INT(11) NOT NULL AUTO_INCREMENT,
  `ISO` VARCHAR(2) NULL DEFAULT NULL COMMENT 'ISO3166-1',
  PRIMARY KEY (`countryID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`ADDRESS`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`ADDRESS` (
  `addressID` INT(11) NOT NULL AUTO_INCREMENT,
  `addrLine` VARCHAR(150) NULL DEFAULT NULL,
  `postBox` VARCHAR(45) NULL DEFAULT NULL,
  `postCode` VARCHAR(45) NULL DEFAULT NULL,
  `settlement` VARCHAR(45) NULL DEFAULT NULL,
  `region` VARCHAR(45) NULL DEFAULT NULL,
  `country` VARCHAR(45) NULL DEFAULT NULL,
  `countryID` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`addressID`),
  INDEX `fk_ADRESS_COUNTRY1_idx` (`countryID` ASC),
  CONSTRAINT `fk_ADRESS_COUNTRY1`
    FOREIGN KEY (`countryID`)
    REFERENCES `anhalytics_biblio`.`COUNTRY` (`countryID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`COLLECTION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`COLLECTION` (
  `collectionID` INT(11) NOT NULL AUTO_INCREMENT,
  `title` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`collectionID`))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`CONFERENCE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`CONFERENCE` (
  `conferenceID` INT(11) NOT NULL AUTO_INCREMENT,
  `title` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`conferenceID`))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`MONOGRAPH`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`MONOGRAPH` (
  `monographID` INT(11) NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(45) NULL DEFAULT NULL COMMENT 'journal \nproceedings\ncollection\nbook\nphd_thesis\nmaster_thesis\nreport\narchive',
  `title` TEXT NULL DEFAULT NULL,
  `shortname` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`monographID`))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`CONFERENCE_EVENT`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`CONFERENCE_EVENT` (
  `conference_eventID` INT(11) NOT NULL AUTO_INCREMENT,
  `conferenceID` INT(11) NOT NULL,
  `addressID` INT(11) NULL DEFAULT NULL,
  `start_date` VARCHAR(45) NULL DEFAULT NULL,
  `end_date` VARCHAR(45) NULL DEFAULT NULL,
  `monographID` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`conference_eventID`),
  INDEX `fk_CONFERENCE_has_ADRESS_ADRESS1_idx` (`addressID` ASC),
  INDEX `fk_CONFERENCE_has_ADRESS_CONFERENCE1_idx` (`conferenceID` ASC),
  INDEX `fk_CONFERENCE_EVENT_MONOGRAPH1_idx` (`monographID` ASC),
  CONSTRAINT `fk_CONFERENCE_EVENT_MONOGRAPH1`
    FOREIGN KEY (`monographID`)
    REFERENCES `anhalytics_biblio`.`MONOGRAPH` (`monographID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_CONFERENCE_has_ADRESS_ADRESS1`
    FOREIGN KEY (`addressID`)
    REFERENCES `anhalytics_biblio`.`ADDRESS` (`addressID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_CONFERENCE_has_ADRESS_CONFERENCE1`
    FOREIGN KEY (`conferenceID`)
    REFERENCES `anhalytics_biblio`.`CONFERENCE` (`conferenceID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`DOCUMENT`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`DOCUMENT` (
  `docID` INT(11) NOT NULL AUTO_INCREMENT,
  `version` VARCHAR(45) NULL DEFAULT NULL,
  `TEImetadatas` LONGTEXT NULL DEFAULT NULL,
  `URI` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`docID`),
  UNIQUE INDEX `URI_UNIQUE` (`URI` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`PERSON`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`PERSON` (
  `personID` INT(11) NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(45) NULL DEFAULT NULL,
  `photo` VARCHAR(45) NULL DEFAULT NULL,
  `url` VARCHAR(150) NULL DEFAULT NULL,
  `email` VARCHAR(150) NULL DEFAULT NULL,
  `phone` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`personID`))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`PUBLISHER`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`PUBLISHER` (
  `publisherID` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(150) NULL DEFAULT NULL,
  PRIMARY KEY (`publisherID`))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`PUBLICATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`PUBLICATION` (
  `publicationID` INT(11) NOT NULL AUTO_INCREMENT,
  `docID` INT(11) NULL DEFAULT NULL,
  `monographID` INT(11) NULL DEFAULT NULL,
  `publisherID` INT(11) NULL DEFAULT NULL,
  `type` VARCHAR(45) NULL DEFAULT NULL COMMENT 'analytics\nmonograph',
  `doc_title` TEXT NULL DEFAULT NULL,
  `date_printed` DATE NULL DEFAULT NULL,
  `date_electronic` VARCHAR(45) NULL DEFAULT NULL,
  `start_page` VARCHAR(45) NULL DEFAULT NULL,
  `end_page` VARCHAR(45) NULL DEFAULT NULL,
  `language` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`publicationID`),
  INDEX `fk_PUBLICATION_DOCUMENT1_idx` (`docID` ASC),
  INDEX `fk_PUBLICATION_MONOGRAPH1_idx` (`monographID` ASC),
  INDEX `fk_PUBLICATION_PUBLISHER1_idx` (`publisherID` ASC),
  CONSTRAINT `fk_PUBLICATION_DOCUMENT1`
    FOREIGN KEY (`docID`)
    REFERENCES `anhalytics_biblio`.`DOCUMENT` (`docID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PUBLICATION_MONOGRAPH1`
    FOREIGN KEY (`monographID`)
    REFERENCES `anhalytics_biblio`.`MONOGRAPH` (`monographID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PUBLICATION_PUBLISHER1`
    FOREIGN KEY (`publisherID`)
    REFERENCES `anhalytics_biblio`.`PUBLISHER` (`publisherID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`EDITOR`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`EDITOR` (
  `rank` INT(11) NULL DEFAULT NULL,
  `personID` INT(11) NULL DEFAULT NULL,
  `publicationID` INT(11) NULL DEFAULT NULL,
  INDEX `fk_EDITOR_PERSON**1_idx` (`personID` ASC),
  INDEX `fk_EDITOR_PUBLICATION1_idx` (`publicationID` ASC),
  CONSTRAINT `fk_EDITOR_PERSON**1`
    FOREIGN KEY (`personID`)
    REFERENCES `anhalytics_biblio`.`PERSON` (`personID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_EDITOR_PUBLICATION1`
    FOREIGN KEY (`publicationID`)
    REFERENCES `anhalytics_biblio`.`PUBLICATION` (`publicationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`JOURNAL`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`JOURNAL` (
  `journalID` INT(11) NOT NULL AUTO_INCREMENT,
  `title` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`journalID`))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`IN_SERIAL`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`IN_SERIAL` (
  `monographID` INT(11) NOT NULL DEFAULT '0',
  `collectionID` INT(11) NULL DEFAULT NULL,
  `journalID` INT(11) NULL DEFAULT NULL,
  `volume` VARCHAR(45) NULL DEFAULT NULL,
  `number` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`monographID`),
  INDEX `fk_MONOGRAPH_has_COLLECTION_COLLECTION1_idx` (`collectionID` ASC),
  INDEX `fk_MONOGRAPH_has_COLLECTION_MONOGRAPH1_idx` (`monographID` ASC),
  INDEX `fk_IN_SERIAL_JOURNAL1_idx` (`journalID` ASC),
  CONSTRAINT `fk_IN_SERIAL_JOURNAL1`
    FOREIGN KEY (`journalID`)
    REFERENCES `anhalytics_biblio`.`JOURNAL` (`journalID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_MONOGRAPH_has_COLLECTION_COLLECTION1`
    FOREIGN KEY (`collectionID`)
    REFERENCES `anhalytics_biblio`.`COLLECTION` (`collectionID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_MONOGRAPH_has_COLLECTION_MONOGRAPH1`
    FOREIGN KEY (`monographID`)
    REFERENCES `anhalytics_biblio`.`MONOGRAPH` (`monographID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`MONOGRAPH_IDENTIFIER`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`MONOGRAPH_IDENTIFIER` (
  `monograph_identifierID` INT(11) NOT NULL AUTO_INCREMENT,
  `monographID` INT(11) NULL DEFAULT NULL,
  `ID` VARCHAR(45) NULL DEFAULT NULL,
  `Type` ENUM('hal','arxiv','doi') NULL DEFAULT NULL COMMENT 'journal \nproceedings\ncollection\nbook\nphd_thesis\nmaster_thesis\nreport\narchive',
  PRIMARY KEY (`monograph_identifierID`),
  INDEX `fk_MONOGRAPH_IDENTIFIER_MONOGRAPH1_idx` (`monographID` ASC),
  CONSTRAINT `fk_MONOGRAPH_IDENTIFIER_MONOGRAPH1`
    FOREIGN KEY (`monographID`)
    REFERENCES `anhalytics_biblio`.`MONOGRAPH` (`monographID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`PERSON_NAME`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`PERSON_NAME` (
  `person_nameID` INT(11) NOT NULL AUTO_INCREMENT,
  `personID` INT(11) NOT NULL,
  `fullname` VARCHAR(150) NULL DEFAULT NULL,
  `forename` VARCHAR(150) NULL DEFAULT NULL,
  `middlename` VARCHAR(45) NULL DEFAULT NULL,
  `surname` VARCHAR(150) NULL DEFAULT NULL,
  `title` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`person_nameID`),
  INDEX `fk_PERSON_NAME_PERSON1_idx` (`personID` ASC),
  CONSTRAINT `fk_PERSON_NAME_PERSON1`
    FOREIGN KEY (`personID`)
    REFERENCES `anhalytics_biblio`.`PERSON` (`personID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`PUBLISHER_LOCATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`PUBLISHER_LOCATION` (
  `publisherID` INT(11) NOT NULL DEFAULT '0',
  `ADRESS_addressID` INT(11) NOT NULL,
  `start_date` VARCHAR(45) NULL DEFAULT NULL,
  `end_date` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`publisherID`, `ADRESS_addressID`),
  INDEX `fk_PUBLISHER_has_ADRESS_ADRESS1_idx` (`ADRESS_addressID` ASC),
  INDEX `fk_PUBLISHER_has_ADRESS_PUBLISHER1_idx` (`publisherID` ASC),
  CONSTRAINT `fk_PUBLISHER_has_ADRESS_ADRESS1`
    FOREIGN KEY (`ADRESS_addressID`)
    REFERENCES `anhalytics_biblio`.`ADDRESS` (`addressID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PUBLISHER_has_ADRESS_PUBLISHER1`
    FOREIGN KEY (`publisherID`)
    REFERENCES `anhalytics_biblio`.`PUBLISHER` (`publisherID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics_biblio`.`SERIAL_IDENTIFIER`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics_biblio`.`SERIAL_IDENTIFIER` (
  `serial_identifierID` INT(11) NOT NULL AUTO_INCREMENT,
  `ID` VARCHAR(45) NULL DEFAULT NULL,
  `Type` ENUM('hal','arxiv','doi') NULL DEFAULT NULL COMMENT 'journal \nproceedings\ncollection\nbook\nphd_thesis\nmaster_thesis\nreport\narchive',
  `journalID` INT(11) NULL DEFAULT NULL,
  `collectionID` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`serial_identifierID`),
  INDEX `fk_SERIAL_IDENTIFIER_JOURNAL1_idx` (`journalID` ASC),
  INDEX `fk_SERIAL_IDENTIFIER_COLLECTION1_idx` (`collectionID` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
