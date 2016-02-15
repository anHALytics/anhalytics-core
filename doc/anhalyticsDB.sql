-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema anhalytics
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema anhalytics
-- -----------------------------------------------------
DROP DATABASE `anhalytics`;

CREATE SCHEMA IF NOT EXISTS `anhalytics` DEFAULT CHARACTER SET utf8 ;
USE `anhalytics` ;

-- -----------------------------------------------------
-- Table `anhalytics`.`COUNTRY`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`COUNTRY` (
  `countryID` INT(11) NOT NULL AUTO_INCREMENT,
  `ISO` VARCHAR(2) NULL DEFAULT NULL COMMENT 'ISO3166-1',
  PRIMARY KEY (`countryID`))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`ADDRESS`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`ADDRESS` (
  `addressID` INT(11) NOT NULL AUTO_INCREMENT,
  `addrLine` TEXT NULL DEFAULT NULL,
  `postBox` VARCHAR(45) NULL DEFAULT NULL,
  `postCode` VARCHAR(45) NULL DEFAULT NULL,
  `settlement` VARCHAR(255) NULL DEFAULT NULL,
  `region` VARCHAR(45) NULL DEFAULT NULL,
  `country` VARCHAR(45) NULL DEFAULT NULL,
  `countryID` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`addressID`),
  INDEX `fk_ADRESS_COUNTRY1_idx` (`countryID` ASC),
  CONSTRAINT `fk_ADRESS_COUNTRY1`
    FOREIGN KEY (`countryID`)
    REFERENCES `anhalytics`.`COUNTRY` (`countryID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`PERSON`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`PERSON` (
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
-- Table `anhalytics`.`ORGANISATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`ORGANISATION` (
  `organisationID` INT(11) NOT NULL AUTO_INCREMENT,
  `type` ENUM('institution','department','laboratory','researchteam') NULL DEFAULT NULL,
  `url` VARCHAR(255) NULL DEFAULT NULL,
  `structID` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`organisationID`),
  UNIQUE INDEX `structID_UNIQUE` (`structID` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`AFFILIATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`AFFILIATION` (
  `affiliationID` INT(11) NOT NULL AUTO_INCREMENT,
  `organisationID` INT(11) NULL DEFAULT NULL,
  `personID` INT(11) NULL DEFAULT NULL,
  `begin_date` DATE NULL DEFAULT NULL,
  `end_date` DATE NULL DEFAULT NULL,
  PRIMARY KEY (`affiliationID`),
  INDEX `fk_table1_STRUTURE1_idx` (`organisationID` ASC),
  INDEX `fk_table1_PERSON1_idx` (`personID` ASC),
  CONSTRAINT `fk_table1_PERSON1`
    FOREIGN KEY (`personID`)
    REFERENCES `anhalytics`.`PERSON` (`personID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_table1_STRUTURE1`
    FOREIGN KEY (`organisationID`)
    REFERENCES `anhalytics`.`ORGANISATION` (`organisationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`DOCUMENT`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`DOCUMENT` (
  `docID` INT(11) NOT NULL AUTO_INCREMENT,
  `version` VARCHAR(45) NULL DEFAULT NULL,
  `TEImetadatas` LONGTEXT NULL DEFAULT NULL,
  `URI` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`docID`))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`AUTHOR`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`AUTHOR` (
  `docID` INT(11) NOT NULL DEFAULT '0',
  `personID` INT(11) NOT NULL DEFAULT '0',
  `rank` INT(11) NULL DEFAULT NULL,
  `corresp` TINYINT(1) NULL DEFAULT NULL,
  PRIMARY KEY (`docID`, `personID`),
  INDEX `fk_authorShip_document1_idx` (`docID` ASC),
  INDEX `fk_authorShip_person1_idx` (`personID` ASC),
  CONSTRAINT `fk_authorShip_document1`
    FOREIGN KEY (`docID`)
    REFERENCES `anhalytics`.`DOCUMENT` (`docID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_authorShip_person1`
    FOREIGN KEY (`personID`)
    REFERENCES `anhalytics`.`PERSON` (`personID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`COLLECTION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`COLLECTION` (
  `collectionID` INT(11) NOT NULL AUTO_INCREMENT,
  `title` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`collectionID`))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`CONFERENCE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`CONFERENCE` (
  `conferenceID` INT(11) NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`conferenceID`))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`MONOGRAPH`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`MONOGRAPH` (
  `monographID` INT(11) NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(45) NULL DEFAULT NULL COMMENT 'journal \nproceedings\ncollection\nbook\nphd_thesis\nmaster_thesis\nreport\narchive',
  `title` TEXT NULL DEFAULT NULL,
  `shortname` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`monographID`))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`CONFERENCE_EVENT`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`CONFERENCE_EVENT` (
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
    REFERENCES `anhalytics`.`MONOGRAPH` (`monographID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_CONFERENCE_has_ADRESS_ADRESS1`
    FOREIGN KEY (`addressID`)
    REFERENCES `anhalytics`.`ADDRESS` (`addressID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_CONFERENCE_has_ADRESS_CONFERENCE1`
    FOREIGN KEY (`conferenceID`)
    REFERENCES `anhalytics`.`CONFERENCE` (`conferenceID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`DOCUMENT_IDENTIFIER`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`DOCUMENT_IDENTIFIER` (
  `document_identifierID` INT(11) NOT NULL AUTO_INCREMENT,
  `docID` INT(11) NOT NULL,
  `ID` VARCHAR(150) NULL DEFAULT NULL,
  `Type` VARCHAR(55) NULL DEFAULT NULL,
  PRIMARY KEY (`document_identifierID`),
  INDEX `fk_IDENTIFIERS_DOCUMENT1_idx` (`docID` ASC),
  CONSTRAINT `fk_IDENTIFIERS_DOCUMENT1`
    FOREIGN KEY (`docID`)
    REFERENCES `anhalytics`.`DOCUMENT` (`docID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`DOCUMENT_ORGANISATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`DOCUMENT_ORGANISATION` (
  `type` ENUM('team','lab','inst') NULL DEFAULT NULL,
  `docID` INT(11) NOT NULL DEFAULT '0',
  `organisationID` INT(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`organisationID`, `docID`),
  INDEX `fk_DOC_structure_document1_idx` (`docID` ASC),
  INDEX `fk_DOC_structure_structure1_idx` (`organisationID` ASC),
  CONSTRAINT `fk_DOC_structure_document1`
    FOREIGN KEY (`docID`)
    REFERENCES `anhalytics`.`DOCUMENT` (`docID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_DOC_structure_structure1`
    FOREIGN KEY (`organisationID`)
    REFERENCES `anhalytics`.`ORGANISATION` (`organisationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`DOMAIN`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`DOMAIN` (
  `publicationID` INT(11) NOT NULL,
  `domain` VARCHAR(150) NULL DEFAULT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`PUBLISHER`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`PUBLISHER` (
  `publisherID` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`publisherID`))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`PUBLICATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`PUBLICATION` (
  `publicationID` INT(11) NOT NULL AUTO_INCREMENT,
  `docID` INT(11) NULL DEFAULT NULL,
  `monographID` INT(11) NULL DEFAULT NULL,
  `publisherID` INT(11) NULL DEFAULT NULL,
  `type` VARCHAR(45) NULL DEFAULT NULL COMMENT 'analytics\nmonograph',
  `doc_title` TEXT NULL DEFAULT NULL,
  `date_printed` DATE NULL DEFAULT NULL,
  `date_electronic` VARCHAR(45) NULL DEFAULT NULL,
  `start_page` VARCHAR(150) NULL DEFAULT NULL,
  `end_page` VARCHAR(45) NULL DEFAULT NULL,
  `language` VARCHAR(45) NULL DEFAULT NULL,
  `domain` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`publicationID`),
  INDEX `fk_PUBLICATION_DOCUMENT1_idx` (`docID` ASC),
  INDEX `fk_PUBLICATION_MONOGRAPH1_idx` (`monographID` ASC),
  INDEX `fk_PUBLICATION_PUBLISHER1_idx` (`publisherID` ASC),
  CONSTRAINT `fk_PUBLICATION_DOCUMENT1`
    FOREIGN KEY (`docID`)
    REFERENCES `anhalytics`.`DOCUMENT` (`docID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PUBLICATION_MONOGRAPH1`
    FOREIGN KEY (`monographID`)
    REFERENCES `anhalytics`.`MONOGRAPH` (`monographID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PUBLICATION_PUBLISHER1`
    FOREIGN KEY (`publisherID`)
    REFERENCES `anhalytics`.`PUBLISHER` (`publisherID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`EDITOR`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`EDITOR` (
  `rank` INT(11) NULL DEFAULT NULL,
  `personID` INT(11) NULL DEFAULT NULL,
  `publicationID` INT(11) NULL DEFAULT NULL,
  INDEX `fk_EDITOR_PERSON**1_idx` (`personID` ASC),
  INDEX `fk_EDITOR_PUBLICATION1_idx` (`publicationID` ASC),
  CONSTRAINT `fk_EDITOR_PERSON**1`
    FOREIGN KEY (`personID`)
    REFERENCES `anhalytics`.`PERSON` (`personID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_EDITOR_PUBLICATION1`
    FOREIGN KEY (`publicationID`)
    REFERENCES `anhalytics`.`PUBLICATION` (`publicationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`JOURNAL`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`JOURNAL` (
  `journalID` INT(11) NOT NULL AUTO_INCREMENT,
  `title` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`journalID`))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`IN_SERIAL`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`IN_SERIAL` (
  `monographID` INT(11) NOT NULL DEFAULT '0',
  `collectionID` INT(11) NULL DEFAULT NULL,
  `journalID` INT(11) NULL DEFAULT NULL,
  `volume` VARCHAR(150) NULL DEFAULT NULL,
  `number` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`monographID`),
  INDEX `fk_MONOGRAPH_has_COLLECTION_COLLECTION1_idx` (`collectionID` ASC),
  INDEX `fk_MONOGRAPH_has_COLLECTION_MONOGRAPH1_idx` (`monographID` ASC),
  INDEX `fk_IN_SERIAL_JOURNAL1_idx` (`journalID` ASC),
  CONSTRAINT `fk_IN_SERIAL_JOURNAL1`
    FOREIGN KEY (`journalID`)
    REFERENCES `anhalytics`.`JOURNAL` (`journalID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_MONOGRAPH_has_COLLECTION_COLLECTION1`
    FOREIGN KEY (`collectionID`)
    REFERENCES `anhalytics`.`COLLECTION` (`collectionID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_MONOGRAPH_has_COLLECTION_MONOGRAPH1`
    FOREIGN KEY (`monographID`)
    REFERENCES `anhalytics`.`MONOGRAPH` (`monographID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`LOCATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`LOCATION` (
  `locationID` INT(11) NOT NULL AUTO_INCREMENT,
  `organisationID` INT(11) NULL DEFAULT NULL,
  `addressID` INT(11) NOT NULL,
  `begin_date` DATE NULL DEFAULT NULL,
  `end_date` DATE NULL DEFAULT NULL,
  PRIMARY KEY (`locationID`),
  INDEX `fk_ADRESS_has_ORGANISATION_ORGANISATION1_idx` (`organisationID` ASC),
  INDEX `fk_ADRESS_has_ORGANISATION_ADRESS1_idx` (`addressID` ASC),
  CONSTRAINT `fk_ADRESS_has_ORGANISATION_ADRESS1`
    FOREIGN KEY (`addressID`)
    REFERENCES `anhalytics`.`ADDRESS` (`addressID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_ADRESS_has_ORGANISATION_ORGANISATION1`
    FOREIGN KEY (`organisationID`)
    REFERENCES `anhalytics`.`ORGANISATION` (`organisationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`MONOGRAPH_IDENTIFIER`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`MONOGRAPH_IDENTIFIER` (
  `monograph_identifierID` INT(11) NOT NULL AUTO_INCREMENT,
  `monographID` INT(11) NULL DEFAULT NULL,
  `ID` VARCHAR(45) NULL DEFAULT NULL,
  `Type` ENUM('hal','arxiv','doi') NULL DEFAULT NULL COMMENT 'journal \nproceedings\ncollection\nbook\nphd_thesis\nmaster_thesis\nreport\narchive',
  PRIMARY KEY (`monograph_identifierID`),
  INDEX `fk_MONOGRAPH_IDENTIFIER_MONOGRAPH1_idx` (`monographID` ASC),
  CONSTRAINT `fk_MONOGRAPH_IDENTIFIER_MONOGRAPH1`
    FOREIGN KEY (`monographID`)
    REFERENCES `anhalytics`.`MONOGRAPH` (`monographID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`ORGANISATION_NAME`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`ORGANISATION_NAME` (
  `organisation_nameID` INT(11) NOT NULL AUTO_INCREMENT,
  `organisationID` INT(11) NOT NULL,
  `name` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`organisation_nameID`),
  INDEX `fk_ORGANISATION_NAME_ORGANISATION1_idx` (`organisationID` ASC),
  CONSTRAINT `fk_ORGANISATION_NAME_ORGANISATION1`
    FOREIGN KEY (`organisationID`)
    REFERENCES `anhalytics`.`ORGANISATION` (`organisationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`PART_OF`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`PART_OF` (
  `organisation_motherID` INT(11) NOT NULL,
  `organisationID` INT(11) NOT NULL,
  `begin_date` DATE NULL DEFAULT NULL,
  `end_date` DATE NULL DEFAULT NULL,
  INDEX `fk_incorporation_structure1_idx` (`organisation_motherID` ASC),
  INDEX `fk_incorporation_structure2_idx` (`organisationID` ASC),
  CONSTRAINT `fk_incorporation_structure1`
    FOREIGN KEY (`organisation_motherID`)
    REFERENCES `anhalytics`.`ORGANISATION` (`organisationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_incorporation_structure2`
    FOREIGN KEY (`organisationID`)
    REFERENCES `anhalytics`.`ORGANISATION` (`organisationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`PERSON_IDENTIFIER`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`PERSON_IDENTIFIER` (
  `person_identifierID` INT(11) NOT NULL AUTO_INCREMENT,
  `personID` INT(11) NOT NULL,
  `ID` VARCHAR(150) NOT NULL,
  `Type` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`person_identifierID`),
  INDEX `fk_PERSON_IDENTIFIERS_PERSON1_idx` (`personID` ASC),
  CONSTRAINT `fk_PERSON_IDENTIFIERS_PERSON1`
    FOREIGN KEY (`personID`)
    REFERENCES `anhalytics`.`PERSON` (`personID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`PERSON_NAME`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`PERSON_NAME` (
  `person_nameID` INT(11) NOT NULL AUTO_INCREMENT,
  `personID` INT(11) NOT NULL,
  `fullname` VARCHAR(255) NULL DEFAULT NULL,
  `forename` VARCHAR(255) NULL DEFAULT NULL,
  `middlename` VARCHAR(45) NULL DEFAULT NULL,
  `surname` VARCHAR(255) NULL DEFAULT NULL,
  `title` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`person_nameID`),
  INDEX `fk_PERSON_NAME_PERSON1_idx` (`personID` ASC),
  CONSTRAINT `fk_PERSON_NAME_PERSON1`
    FOREIGN KEY (`personID`)
    REFERENCES `anhalytics`.`PERSON` (`personID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`PUBLISHER_LOCATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`PUBLISHER_LOCATION` (
  `publisherID` INT(11) NOT NULL DEFAULT '0',
  `ADRESS_addressID` INT(11) NOT NULL,
  `start_date` VARCHAR(45) NULL DEFAULT NULL,
  `end_date` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`publisherID`, `ADRESS_addressID`),
  INDEX `fk_PUBLISHER_has_ADRESS_ADRESS1_idx` (`ADRESS_addressID` ASC),
  INDEX `fk_PUBLISHER_has_ADRESS_PUBLISHER1_idx` (`publisherID` ASC),
  CONSTRAINT `fk_PUBLISHER_has_ADRESS_ADRESS1`
    FOREIGN KEY (`ADRESS_addressID`)
    REFERENCES `anhalytics`.`ADDRESS` (`addressID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PUBLISHER_has_ADRESS_PUBLISHER1`
    FOREIGN KEY (`publisherID`)
    REFERENCES `anhalytics`.`PUBLISHER` (`publisherID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`REFERENCE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`REFERENCE` (
  `citingDocID` INT(11) NOT NULL DEFAULT '0',
  `citedDocID` INT(11) NOT NULL DEFAULT '0',
  `citation_text` BLOB NULL DEFAULT NULL,
  PRIMARY KEY (`citingDocID`, `citedDocID`),
  INDEX `fk_DOCUMENT_has_DOCUMENT_DOCUMENT2_idx` (`citedDocID` ASC),
  INDEX `fk_DOCUMENT_has_DOCUMENT_DOCUMENT1_idx` (`citingDocID` ASC),
  CONSTRAINT `fk_DOCUMENT_has_DOCUMENT_DOCUMENT1`
    FOREIGN KEY (`citingDocID`)
    REFERENCES `anhalytics`.`DOCUMENT` (`docID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_DOCUMENT_has_DOCUMENT_DOCUMENT2`
    FOREIGN KEY (`citedDocID`)
    REFERENCES `anhalytics`.`DOCUMENT` (`docID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `anhalytics`.`SERIAL_IDENTIFIER`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `anhalytics`.`SERIAL_IDENTIFIER` (
  `serial_identifierID` INT(11) NOT NULL AUTO_INCREMENT,
  `ID` VARCHAR(45) NULL DEFAULT NULL,
  `Type` ENUM('hal','arxiv','doi') NULL DEFAULT NULL COMMENT 'journal \nproceedings\ncollection\nbook\nphd_thesis\nmaster_thesis\nreport\narchive',
  `journalID` INT(11) NULL DEFAULT NULL,
  `collectionID` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`serial_identifierID`),
  INDEX `fk_SERIAL_IDENTIFIER_JOURNAL1_idx` (`journalID` ASC),
  INDEX `fk_SERIAL_IDENTIFIER_COLLECTION1_idx` (`collectionID` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 0
DEFAULT CHARACTER SET = utf8;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
