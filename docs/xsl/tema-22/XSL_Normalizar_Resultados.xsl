
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:multimap="http://sap.com/xi/XI/SplitAndMerge"
    exclude-result-prefixes="multimap">

    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

    <xsl:template match="/">
        <Results>
            <xsl:for-each select="/multimap:Messages/multimap:Message1/Result">
                <Result>
                    <OrderId>
                        <xsl:value-of select="OrderId"/>
                    </OrderId>
                    <Recipient>
                        <xsl:value-of select="Recipient"/>
                    </Recipient>
                    <Status>
                        <xsl:value-of select="Status"/>
                    </Status>
                </Result>
            </xsl:for-each>
        </Results>
    </xsl:template>

</xsl:stylesheet>
