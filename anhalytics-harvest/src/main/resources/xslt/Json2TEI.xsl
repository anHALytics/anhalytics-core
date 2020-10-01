<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs"
                xpath-default-namespace="http://www.w3.org/2005/xpath-functions" xmlns="http://www.tei-c.org/ns/1.0"
                expand-text="yes">
    <!-- Source: https://ralph.blog.imixs.com/2019/08/05/how-to-convert-json-to-xml/ -->
    <xsl:output method="xml" indent="yes"/>
    <xsl:mode on-no-match="shallow-copy"/>

    <xsl:param name="text-encoding" as="xs:string" select="'utf-8'"/>
    <xsl:param name="text" as="xs:string"/>

    <xsl:template match="/" name="init">
        <xsl:variable name="sourcejson" select="json-to-xml($text)"/>

        <TEI xml:lang="en">
            <teiHeader>
                <fileDesc>
                    <titleStmt>
                        <title>
                            <xsl:apply-templates select="$sourcejson/map/string[@key='title']"/>
                        </title>
                    </titleStmt>
                    <editionStmt>
                        <xsl:apply-templates select="$sourcejson/array[@key = 'versions']"/>
                        <xsl:apply-templates select="$sourcejson/string[@key = 'update_date']"/>
                        <respStmt>
                            <resp>contributor</resp>
                            <xsl:apply-templates select="$sourcejson/string[@key = 'submitter']"/>
                        </respStmt>
                    </editionStmt>
                    <publicationStmt>
                        <distributor>arXiv</distributor>
                        <xsl:apply-templates select="$sourcejson/string[@key = 'id']"/>
                    </publicationStmt>
                    <sourceDesc>
                        <biblStruct>
                            <analytic>
                                <xsl:apply-templates select="$sourcejson/map/array[@key='authors_parsed']"/>
                                <xsl:apply-templates select="$sourcejson/map/string[@key='title']"/>
                                <xsl:apply-templates select="$sourcejson/map/string[@key = 'doi']"/>
                            </analytic>
                            <monogr>
                                <xsl:apply-templates select="$sourcejson/map/string[@key = 'journal-ref']"/>
                            </monogr>
                        </biblStruct>
                    </sourceDesc>
                </fileDesc>


                <profileDesc>
                    <xsl:apply-templates select="$sourcejson/map/string[@key = 'abstract']"/>
                </profileDesc>
            </teiHeader>
        </TEI>
    </xsl:template>

    <xsl:template match="string[@key = 'id']">
        <idno type="arXivId">
            <xsl:apply-templates/>
        </idno>
    </xsl:template>

    <xsl:template match="string[@key = 'abstract']">
        <abstract>
            <p>
                <xsl:apply-templates/>
            </p>
        </abstract>
    </xsl:template>

    <xsl:template match="string[@key = 'title']">
        <xsl:if test="normalize-space()">
            <title level="a" type="main">
                <xsl:apply-templates/>
            </title>
        </xsl:if>
    </xsl:template>

    <xsl:template match="array[@key = 'authors_parsed']">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="string[@key = 'update_date']">
        <date type="whenModified" when="{.}"/>
    </xsl:template>
    <xsl:template match="number[@key = 'year']">
        <date type="published" when="{.}"/>
    </xsl:template>

    <xsl:template match="string[@key = 'journal-ref']">
        <title level="j">
            <xsl:apply-templates/>
        </title>
    </xsl:template>

    <xsl:template match="string[@key = 'volume']">
        <xsl:if test="normalize-space()">
            <biblScope unit="volume">
                <xsl:apply-templates/>
            </biblScope>
        </xsl:if>
    </xsl:template>

    <xsl:template match="string[@key = 'issn']">
        <xsl:if test="normalize-space()">
            <idno type="issn">
                <xsl:apply-templates/>
            </idno>
        </xsl:if>
    </xsl:template>

    <xsl:template match="string[@key = 'pages']">
        <biblScope unit="page">
            <xsl:apply-templates/>
        </biblScope>
    </xsl:template>

    <xsl:template match="null"/>

    <xsl:template match="map[@key = 'other_ids']">
        <xsl:for-each select="array">
            <xsl:if test="normalize-space()">
                <idno type="{@key}">
                    <xsl:value-of select="."/>
                </idno>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="string[@key = 'ref_id']">
        <idno type="ref_id">
            <xsl:apply-templates/>
        </idno>
    </xsl:template>

    <!-- Dealing with authors -->

    <xsl:template match="array[@key = 'authors_parsed']/array">
        <author>
            <persName>
                <forename type="first">
                    <xsl:value-of select="string[2]"/>
                </forename>
                <xsl:if test="normalize-space(string[3]) != ''">
                    <forename type="middle">
                        <xsl:value-of select="string[3]"/>
                    </forename>
                </xsl:if>
                <surname>
                    <xsl:value-of select="string[1]"/>
                </surname>
            </persName>
            <xsl:apply-templates select="string[@key = 'suffix'] | map[@key = 'affiliation']"/>
        </author>
    </xsl:template>

    <xsl:template match="string[@key = 'submitter']">
        <persName>
            <xsl:apply-templates/>
        </persName>
    </xsl:template>

    <xsl:template match="string[@key = 'doi']">
        <xsl:if test="normalize-space()">
            <idno type="doi">
                <xsl:apply-templates/>
            </idno>
        </xsl:if>
    </xsl:template>

    <xsl:template match="string[@key = 'email']">
        <xsl:if test="normalize-space()">
            <email>
                <xsl:apply-templates/>
            </email>
        </xsl:if>
    </xsl:template>

    <xsl:template match="map[@key = 'affiliation']">
        <affiliation>
            <xsl:apply-templates/>
        </affiliation>
    </xsl:template>

    <xsl:template match="string[@key = 'laboratory']">
        <xsl:if test="normalize-space()">
            <orgName type="laboratory">
                <xsl:apply-templates/>
            </orgName>
        </xsl:if>
    </xsl:template>

    <xsl:template match="string[@key = 'institution']">
        <xsl:if test="normalize-space()">
            <orgName type="institution">
                <xsl:apply-templates/>
            </orgName>
        </xsl:if>
    </xsl:template>

    <xsl:template match="map[@key = 'location']">
        <address>
            <xsl:apply-templates/>
        </address>
    </xsl:template>

    <xsl:template match="string[@key = 'addrLine']">
        <xsl:if test="normalize-space()">
            <addrLine>
                <xsl:apply-templates/>
            </addrLine>
        </xsl:if>
    </xsl:template>

    <xsl:template match="string[@key = 'postCode']">
        <xsl:if test="normalize-space()">
            <postCode>
                <xsl:apply-templates/>
            </postCode>
        </xsl:if>
    </xsl:template>

    <xsl:template match="string[@key = 'settlement']">
        <xsl:if test="normalize-space()">
            <settlement>
                <xsl:apply-templates/>
            </settlement>
        </xsl:if>
    </xsl:template>

    <xsl:template match="string[@key = 'region']">
        <xsl:if test="normalize-space()">
            <region>
                <xsl:apply-templates/>
            </region>
        </xsl:if>
    </xsl:template>

    <xsl:template match="string[@key = 'country']">
        <xsl:if test="normalize-space()">
            <country>
                <xsl:apply-templates/>
            </country>
        </xsl:if>
    </xsl:template>


    <xsl:template match="array[@key = 'versions']">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="array[@key = 'versions']/map">
        <edition n="{string[@key='version']}">
            <date type="whenCreated">
                <xsl:value-of select="string[@key = 'created']"/>
            </date>
        </edition>
    </xsl:template>

</xsl:stylesheet>
