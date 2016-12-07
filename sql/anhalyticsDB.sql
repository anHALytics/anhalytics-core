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
-- CREATE SCHEMA IF NOT EXISTS `anhalytics` DEFAULT CHARACTER SET utf8 ;
--  USE `anhalytics2` ;

-- -----------------------------------------------------
-- Table `COUNTRY`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `COUNTRY` (
  `countryID` INT(11) NOT NULL AUTO_INCREMENT,
  `ISO` VARCHAR(2) NULL DEFAULT NULL COMMENT 'ISO3166-1',
  PRIMARY KEY (`countryID`),
  UNIQUE INDEX `ISO_UNIQUE` (`ISO` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `ADDRESS`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ADDRESS` (
  `addressID` INT(11) NOT NULL AUTO_INCREMENT,
  `addrLine` VARCHAR(150) NULL DEFAULT NULL,
  `postBox` VARCHAR(45) NULL DEFAULT NULL,
  `postCode` VARCHAR(45) NULL DEFAULT NULL,
  `settlement` VARCHAR(45) NULL DEFAULT NULL,
  `region` VARCHAR(45) NULL DEFAULT NULL,
  `countryID` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`addressID`),
  INDEX `fk_ADRESS_COUNTRY1_idx` (`countryID` ASC),
  CONSTRAINT `fk_ADRESS_COUNTRY1`
    FOREIGN KEY (`countryID`)
    REFERENCES `COUNTRY` (`countryID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `PERSON`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `PERSON` (
  `personID` INT(11) NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(45) NULL DEFAULT NULL,
  `photo` VARCHAR(45) NULL DEFAULT NULL,
  `url` VARCHAR(150) NULL DEFAULT NULL,
  `email` VARCHAR(150) NULL DEFAULT NULL,
  `phone` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`personID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `ORGANISATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ORGANISATION` (
  `organisationID` INT(11) NOT NULL AUTO_INCREMENT,
  `type` ENUM('institution', 'department', 'laboratory', 'researchteam') NULL DEFAULT NULL,
  `url` VARCHAR(255) NULL DEFAULT NULL,
  `status` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`organisationID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `AFFILIATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `AFFILIATION` (
  `affiliationID` INT(11) NOT NULL AUTO_INCREMENT,
  `organisationID` INT(11) NULL DEFAULT NULL,
  `personID` INT(11) NULL DEFAULT NULL,
  `from_date` DATE NULL DEFAULT NULL,
  `until_date` DATE NULL DEFAULT NULL,
  PRIMARY KEY (`affiliationID`),
  UNIQUE INDEX `index4` (`organisationID` ASC, `personID` ASC, `from_date` ASC),
  INDEX `fk_table1_STRUTURE1_idx` (`organisationID` ASC),
  INDEX `fk_table1_PERSON1_idx` (`personID` ASC),
  CONSTRAINT `fk_table1_PERSON1`
    FOREIGN KEY (`personID`)
    REFERENCES `PERSON` (`personID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_table1_STRUTURE1`
    FOREIGN KEY (`organisationID`)
    REFERENCES `ORGANISATION` (`organisationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `DOCUMENT`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `DOCUMENT` (
  `docID` VARCHAR(45) NOT NULL,
  `version` VARCHAR(45) NULL DEFAULT NULL,
  `URI` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`docID`),
  UNIQUE INDEX `URI_UNIQUE` (`URI` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `AUTHORSHIP`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `AUTHORSHIP` (
  `docID` VARCHAR(45) NOT NULL,
  `personID` INT(11) NOT NULL DEFAULT '0',
  `rank` INT(11) NULL DEFAULT NULL,
  `corresp` TINYINT(1) NULL DEFAULT NULL,
  PRIMARY KEY (`docID`, `personID`),
  INDEX `fk_authorShip_person1_idx` (`personID` ASC),
  CONSTRAINT `fk_authorShip_document1`
    FOREIGN KEY (`docID`)
    REFERENCES `DOCUMENT` (`docID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_authorShip_person1`
    FOREIGN KEY (`personID`)
    REFERENCES `PERSON` (`personID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `COLLECTION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `COLLECTION` (
  `collectionID` INT(11) NOT NULL AUTO_INCREMENT,
  `title` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`collectionID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `CONFERENCE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `CONFERENCE` (
  `conferenceID` INT(11) NOT NULL AUTO_INCREMENT,
  `title` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`conferenceID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `MONOGRAPH`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `MONOGRAPH` (
  `monographID` INT(11) NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(45) NULL DEFAULT NULL COMMENT 'journal \nproceedings\ncollection\nbook\nphd_thesis\nmaster_thesis\nreport\narchive',
  `title` TEXT NULL DEFAULT NULL,
  `shortname` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`monographID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `CONFERENCE_EVENT`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `CONFERENCE_EVENT` (
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
    REFERENCES `MONOGRAPH` (`monographID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_CONFERENCE_has_ADRESS_ADRESS1`
    FOREIGN KEY (`addressID`)
    REFERENCES `ADDRESS` (`addressID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_CONFERENCE_has_ADRESS_CONFERENCE1`
    FOREIGN KEY (`conferenceID`)
    REFERENCES `CONFERENCE` (`conferenceID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `DOCUMENT_IDENTIFIER`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `DOCUMENT_IDENTIFIER` (
  `document_identifierID` INT(11) NOT NULL AUTO_INCREMENT,
  `docID` VARCHAR(45) NOT NULL,
  `ID` VARCHAR(150) NULL DEFAULT NULL,
  `Type` VARCHAR(55) NULL DEFAULT NULL,
  PRIMARY KEY (`document_identifierID`),
  UNIQUE INDEX `index3` (`docID` ASC, `ID` ASC, `Type` ASC),
  INDEX `fk_IDENTIFIERS_DOCUMENT1` (`docID` ASC),
  CONSTRAINT `fk_IDENTIFIERS_DOCUMENT1`
    FOREIGN KEY (`docID`)
    REFERENCES `DOCUMENT` (`docID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `DOCUMENT_ORGANISATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `DOCUMENT_ORGANISATION` (
  `docID` VARCHAR(45) NOT NULL,
  `organisationID` INT(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`organisationID`, `docID`),
  INDEX `fk_DOC_structure_structure1_idx` (`organisationID` ASC),
  INDEX `fk_DOC_structure_document1` (`docID` ASC),
  CONSTRAINT `fk_DOC_structure_document1`
    FOREIGN KEY (`docID`)
    REFERENCES `DOCUMENT` (`docID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_DOC_structure_structure1`
    FOREIGN KEY (`organisationID`)
    REFERENCES `ORGANISATION` (`organisationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `DOMAIN`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `DOMAIN` (
  `publicationID` INT(11) NOT NULL,
  `domain` VARCHAR(150) NULL DEFAULT NULL)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `PUBLISHER`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `PUBLISHER` (
  `publisherID` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(150) NULL DEFAULT NULL,
  PRIMARY KEY (`publisherID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `PUBLICATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `PUBLICATION` (
  `publicationID` INT(11) NOT NULL AUTO_INCREMENT,
  `docID` VARCHAR(45) NOT NULL,
  `monographID` INT(11) NULL DEFAULT NULL,
  `publisherID` INT(11) NULL DEFAULT NULL,
  `type` VARCHAR(45) NULL DEFAULT NULL COMMENT 'analytics\nmonograph',
  `doc_title` TEXT NULL DEFAULT NULL,
  `date_printed` DATE NULL DEFAULT NULL,
  `date_electronic` VARCHAR(45) NULL DEFAULT NULL,
  `first_page` VARCHAR(45) NULL DEFAULT NULL,
  `last_page` VARCHAR(45) NULL DEFAULT NULL,
  `language` VARCHAR(45) NULL DEFAULT NULL,
  `domain` VARCHAR(150) NULL DEFAULT NULL,
  PRIMARY KEY (`publicationID`),
  INDEX `fk_PUBLICATION_MONOGRAPH1_idx` (`monographID` ASC),
  INDEX `fk_PUBLICATION_PUBLISHER1_idx` (`publisherID` ASC),
  INDEX `fk_PUBLICATION_DOCUMENT1` (`docID` ASC),
  CONSTRAINT `fk_PUBLICATION_DOCUMENT1`
    FOREIGN KEY (`docID`)
    REFERENCES `DOCUMENT` (`docID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PUBLICATION_MONOGRAPH1`
    FOREIGN KEY (`monographID`)
    REFERENCES `MONOGRAPH` (`monographID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PUBLICATION_PUBLISHER1`
    FOREIGN KEY (`publisherID`)
    REFERENCES `PUBLISHER` (`publisherID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `EDITORSHIP`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `EDITORSHIP` (
  `rank` INT(11) NULL DEFAULT NULL,
  `personID` INT(11) NULL DEFAULT NULL,
  `publicationID` INT(11) NULL DEFAULT NULL,
  INDEX `fk_EDITOR_PERSON**1_idx` (`personID` ASC),
  INDEX `fk_EDITOR_PUBLICATION1_idx` (`publicationID` ASC),
  CONSTRAINT `fk_EDITOR_PERSON**1`
    FOREIGN KEY (`personID`)
    REFERENCES `PERSON` (`personID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_EDITOR_PUBLICATION1`
    FOREIGN KEY (`publicationID`)
    REFERENCES `PUBLICATION` (`publicationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `JOURNAL`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `JOURNAL` (
  `journalID` INT(11) NOT NULL AUTO_INCREMENT,
  `title` TEXT NULL DEFAULT NULL,
  PRIMARY KEY (`journalID`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `IN_SERIAL`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `IN_SERIAL` (
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
    REFERENCES `JOURNAL` (`journalID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_MONOGRAPH_has_COLLECTION_COLLECTION1`
    FOREIGN KEY (`collectionID`)
    REFERENCES `COLLECTION` (`collectionID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_MONOGRAPH_has_COLLECTION_MONOGRAPH1`
    FOREIGN KEY (`monographID`)
    REFERENCES `MONOGRAPH` (`monographID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `LOCATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `LOCATION` (
  `locationID` INT(11) NOT NULL AUTO_INCREMENT,
  `organisationID` INT(11) NULL DEFAULT NULL,
  `addressID` INT(11) NOT NULL,
  `from_date` DATE NULL DEFAULT NULL,
  `until_date` DATE NULL DEFAULT NULL,
  PRIMARY KEY (`locationID`),
  UNIQUE INDEX `index4` (`organisationID` ASC, `addressID` ASC, `from_date` ASC),
  INDEX `fk_ADRESS_has_ORGANISATION_ORGANISATION1_idx` (`organisationID` ASC),
  INDEX `fk_ADRESS_has_ORGANISATION_ADRESS1_idx` (`addressID` ASC),
  CONSTRAINT `fk_ADRESS_has_ORGANISATION_ADRESS1`
    FOREIGN KEY (`addressID`)
    REFERENCES `ADDRESS` (`addressID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_ADRESS_has_ORGANISATION_ORGANISATION1`
    FOREIGN KEY (`organisationID`)
    REFERENCES `ORGANISATION` (`organisationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `MONOGRAPH_IDENTIFIER`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `MONOGRAPH_IDENTIFIER` (
  `monograph_identifierID` INT(11) NOT NULL AUTO_INCREMENT,
  `monographID` INT(11) NULL DEFAULT NULL,
  `ID` VARCHAR(45) NULL DEFAULT NULL,
  `Type` ENUM('hal', 'arxiv', 'doi') NULL DEFAULT NULL COMMENT 'journal \nproceedings\ncollection\nbook\nphd_thesis\nmaster_thesis\nreport\narchive',
  PRIMARY KEY (`monograph_identifierID`),
  INDEX `fk_MONOGRAPH_IDENTIFIER_MONOGRAPH1_idx` (`monographID` ASC),
  CONSTRAINT `fk_MONOGRAPH_IDENTIFIER_MONOGRAPH1`
    FOREIGN KEY (`monographID`)
    REFERENCES `MONOGRAPH` (`monographID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `ORGANISATION_IDENTIFIER`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ORGANISATION_IDENTIFIER` (
  `organisation_identifierID` INT(11) NOT NULL AUTO_INCREMENT,
  `organisationID` INT(11) NOT NULL,
  `ID` VARCHAR(150) NULL DEFAULT NULL,
  `Type` VARCHAR(55) NULL DEFAULT NULL,
  PRIMARY KEY (`organisation_identifierID`),
  UNIQUE INDEX `index3` (`organisationID` ASC, `ID` ASC, `Type` ASC),
  INDEX `fk_IDENTIFIERS_DOCUMENT1` (`organisationID` ASC),
  CONSTRAINT `fk_IDENTIFIERS_ORGANISATION1`
    FOREIGN KEY (`organisationID`)
    REFERENCES `ORGANISATION` (`organisationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `ORGANISATION_NAME`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ORGANISATION_NAME` (
  `organisation_nameID` INT(11) NOT NULL AUTO_INCREMENT,
  `organisationID` INT(11) NOT NULL,
  `name` VARCHAR(150) NULL DEFAULT NULL,
  `lastupdate_date` DATE NULL DEFAULT NULL,
  PRIMARY KEY (`organisation_nameID`),
  UNIQUE INDEX `index3` (`organisationID` ASC, `name` ASC, `lastupdate_date` ASC),
  INDEX `fk_ORGANISATION_NAME_ORGANISATION1_idx` (`organisationID` ASC),
  CONSTRAINT `fk_ORGANISATION_NAME_ORGANISATION1`
    FOREIGN KEY (`organisationID`)
    REFERENCES `ORGANISATION` (`organisationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `PART_OF`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `PART_OF` (
  `organisation_motherID` INT(11) NOT NULL,
  `organisationID` INT(11) NOT NULL,
  `from_date` DATE NULL DEFAULT NULL,
  `until_date` DATE NULL DEFAULT NULL,
  UNIQUE INDEX `index3` (`organisationID` ASC, `from_date` ASC, `organisation_motherID` ASC),
  INDEX `fk_incorporation_structure1_idx` (`organisation_motherID` ASC),
  INDEX `fk_incorporation_structure2_idx` (`organisationID` ASC),
  CONSTRAINT `fk_incorporation_structure1`
    FOREIGN KEY (`organisation_motherID`)
    REFERENCES `ORGANISATION` (`organisationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_incorporation_structure2`
    FOREIGN KEY (`organisationID`)
    REFERENCES `ORGANISATION` (`organisationID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `PERSON_IDENTIFIER`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `PERSON_IDENTIFIER` (
  `person_identifierID` INT(11) NOT NULL AUTO_INCREMENT,
  `personID` INT(11) NOT NULL,
  `ID` VARCHAR(150) NOT NULL,
  `Type` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`person_identifierID`),
  UNIQUE INDEX `index3` (`personID` ASC, `ID` ASC, `Type` ASC),
  INDEX `fk_PERSON_IDENTIFIERS_PERSON1_idx` (`personID` ASC),
  CONSTRAINT `fk_PERSON_IDENTIFIERS_PERSON1`
    FOREIGN KEY (`personID`)
    REFERENCES `PERSON` (`personID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `PERSON_NAME`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `PERSON_NAME` (
  `person_nameID` INT(11) NOT NULL AUTO_INCREMENT,
  `personID` INT(11) NOT NULL,
  `fullname` VARCHAR(150) NULL DEFAULT NULL,
  `forename` VARCHAR(150) NULL DEFAULT NULL,
  `middlename` VARCHAR(45) NULL DEFAULT NULL,
  `surname` VARCHAR(150) NULL DEFAULT NULL,
  `title` VARCHAR(45) NULL DEFAULT NULL,
  `lastupdate_date` DATE NULL DEFAULT NULL,
  PRIMARY KEY (`person_nameID`),
  UNIQUE INDEX `index3` (`personID` ASC, `lastupdate_date` ASC),
  INDEX `fk_PERSON_NAME_PERSON1_idx` (`personID` ASC),
  CONSTRAINT `fk_PERSON_NAME_PERSON1`
    FOREIGN KEY (`personID`)
    REFERENCES `PERSON` (`personID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `PUBLISHER_LOCATION`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `PUBLISHER_LOCATION` (
  `publisherID` INT(11) NOT NULL DEFAULT '0',
  `ADRESS_addressID` INT(11) NOT NULL,
  `from_date` VARCHAR(45) NULL DEFAULT NULL,
  `until_date` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`publisherID`, `ADRESS_addressID`),
  INDEX `fk_PUBLISHER_has_ADRESS_ADRESS1_idx` (`ADRESS_addressID` ASC),
  INDEX `fk_PUBLISHER_has_ADRESS_PUBLISHER1_idx` (`publisherID` ASC),
  CONSTRAINT `fk_PUBLISHER_has_ADRESS_ADRESS1`
    FOREIGN KEY (`ADRESS_addressID`)
    REFERENCES `ADDRESS` (`addressID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_PUBLISHER_has_ADRESS_PUBLISHER1`
    FOREIGN KEY (`publisherID`)
    REFERENCES `PUBLISHER` (`publisherID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `REFERENCE`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `REFERENCE` (
  `citingDocID` VARCHAR(45) NOT NULL,
  `citedDocID` VARCHAR(45) NOT NULL,
  `citation_text` BLOB NULL DEFAULT NULL,
  PRIMARY KEY (`citingDocID`, `citedDocID`),
  INDEX `fk_DOCUMENT_has_DOCUMENT_DOCUMENT2_idx` (`citedDocID` ASC),
  INDEX `fk_DOCUMENT_has_DOCUMENT_DOCUMENT1_idx` (`citingDocID` ASC),
  CONSTRAINT `fk_DOCUMENT_has_DOCUMENT_DOCUMENT1`
    FOREIGN KEY (`citingDocID`)
    REFERENCES `DOCUMENT` (`docID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_DOCUMENT_has_DOCUMENT_DOCUMENT2`
    FOREIGN KEY (`citedDocID`)
    REFERENCES `DOCUMENT` (`docID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `SERIAL_IDENTIFIER`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `SERIAL_IDENTIFIER` (
  `serial_identifierID` INT(11) NOT NULL AUTO_INCREMENT,
  `ID` VARCHAR(45) NULL DEFAULT NULL,
  `Type` VARCHAR(45) NULL DEFAULT NULL,
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
