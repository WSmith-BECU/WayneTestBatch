package com.blueeaglecreditunion.scrpits;

public class queries {

    public static String queryString(){

        return " WITH DATES (StartDate, EndDate) AS (\n" +
                "    SELECT\n" +
                "        COALESCE('2022-08-01', ENV.POSTING_DATE +1 DAY - DAY(ENV.POSTING_DATE) DAYS) StartDate,\n" +
                "        COALESCE('2022-08-31', ENV.POSTING_DATE) EndDate\n" +
                "    FROM CORE.ENV ENV\n" +
                " ),\n" +
                " \n" +
                " \n" +
                "     MEMBERSHIP AS\n" +
                "         (\n" +
                "             SELECT\n" +
                "                 *\n" +
                "             FROM\n" +
                "                 (\n" +
                "                     SELECT\n" +
                "                         person.serial person_serial,\n" +
                "                         person.last_name AS last_name,\n" +
                "                         person.first_name AS first_name,\n" +
                "                         person.MIDDLE_NAME AS middle_name,\n" +
                "                         account.account_number,\n" +
                "                         dense_rank() OVER (\n" +
                "                             partition BY person.serial\n" +
                "                             ORDER BY\n" +
                "                                 share.serial\n" +
                "                             ) \"Membership #\",\n" +
                "                         COALESCE(person.BIRTH_DATE, current_date) DOB,\n" +
                "                         share.open_date AS OPEN_DATE,\n" +
                "                         share.close_date\n" +
                " \n" +
                "                     FROM\n" +
                "                         core.person person\n" +
                "                             INNER JOIN core.share share ON share.tax_person_serial = person.serial AND share.type_serial = 2 -- Primary membership savings\n" +
                "                             INNER JOIN core.account account ON account.serial = share.parent_serial\n" +
                "                             CROSS JOIN dates\n" +
                "                     WHERE\n" +
                "                         share.close_date IS NULL -- Member cannot have opened and closed their S1 in the same month. They aren't technically a new member anymore.\n" +
                "                       -- Note that the WHERE clause runs before the DENSE_RANK() window function. We're ranking all S1s opened this month to\n" +
                "                       -- get the first one and only show that first one.\n" +
                "                       AND share.open_date BETWEEN startdate\n" +
                "                         AND enddate -- This first S1 must have been opened this month (StartDate and EndDate is always the current month)\n" +
                "                       AND NOT EXISTS (\n" +
                "                             SELECT 1\n" +
                "                             FROM\n" +
                "                                 CORE.PERSON ACTIVE_PERSON INNER JOIN\n" +
                "                                 CORE.SHARE ACTIVE_SHARE ON\n" +
                "                                             ACTIVE_PERSON.SERIAL = ACTIVE_SHARE.TAX_PERSON_SERIAL\n" +
                "                                         AND ACTIVE_SHARE.TYPE_SERIAL = 2\n" +
                "                                         AND (ACTIVE_SHARE.OPEN_DATE<SHARE.OPEN_DATE)\n" +
                "                                         AND (COALESCE(ACTIVE_SHARE.CLOSE_DATE, SHARE.OPEN_DATE + 1 DAY) >= SHARE.OPEN_DATE)\n" +
                "                             WHERE ACTIVE_PERSON.SERIAL = PERSON.serial) -- Person cannot be in the PREVIOUS_MONTH cte\n" +
                "                 )\n" +
                "             WHERE\n" +
                "                     \"Membership #\" = 1\n" +
                "         ),\n" +
                " \n" +
                "     CurrentAddress(personSerial,street,additionalAddressLine,city,state,postalCode,combinedAddress) AS\n" +
                "         (\n" +
                "             SELECT PERSON_SERIAL,\n" +
                "                    STREET,\n" +
                "                    ADDITIONAL_ADDRESS_LINE,\n" +
                "                    CITY,\n" +
                "                    STATE,\n" +
                "                    POSTAL_CODE,\n" +
                "                    COMBINED_ADDRESS\n" +
                "             FROM (\n" +
                "                      SELECT PERSON.SERIAL                                                   AS PERSON_SERIAL,\n" +
                "                             COALESCE(PAL.ORDINAL, 0)                                        AS PAL_ORDINAL,\n" +
                "                             MIN(COALESCE(PAL.ORDINAL, 0)) OVER (PARTITION BY PERSON.SERIAL) AS MIN_PAL_ORDINAL,\n" +
                "                             COALESCE(ADDRESS.ADDITIONAL_ADDRESS_LINE, '')                   AS ADDITIONAL_ADDRESS_LINE,\n" +
                "                             COALESCE(ADDRESS.STREET, '')                                    AS STREET,\n" +
                "                             COALESCE(ADDRESS.CITY, '')                                      AS CITY,\n" +
                "                             COALESCE(ADDRESS.STATE, '')                                     AS STATE,\n" +
                "                             COALESCE(ADDRESS.POSTAL_CODE, '')                               AS POSTAL_CODE,\n" +
                "                             COALESCE(ADDRESS.STREET || ' ', '') ||\n" +
                "                             COALESCE(ADDRESS.ADDITIONAL_ADDRESS_LINE || ', ', '') ||\n" +
                "                             COALESCE(ADDRESS.CITY || ' ', '') || COALESCE(ADDRESS.STATE, '') ||\n" +
                "                             COALESCE(', ' || ADDRESS.POSTAL_CODE, '')                       AS COMBINED_ADDRESS\n" +
                "                      FROM CORE.PERSON AS PERSON\n" +
                "                               INNER JOIN CORE.ENV AS ENV ON ENV.SERIAL > 0\n" +
                "                               LEFT OUTER JOIN\n" +
                "                           (CORE.PERSON_ADDRESS_LINK AS PAL INNER JOIN\n" +
                "                               CORE.ADDRESS AS ADDRESS ON\n" +
                "                                       PAL.ADDRESS_SERIAL = ADDRESS.SERIAL) ON\n" +
                "                                       PAL.PARENT_SERIAL = PERSON.SERIAL AND\n" +
                "                                       COALESCE(PAL.EFFECTIVE_DATE, '1800-01-01') <= ENV.POSTING_DATE AND\n" +
                "                                       COALESCE(PAL.EXPIRATION_DATE, '2999-12-31') >= ENV.POSTING_DATE AND\n" +
                "                                       PAL.BAD_ADDRESS <> 'Y' AND\n" +
                "                                       PAL.CATEGORY <> 'P' /* Previous */\n" +
                "                  ) a\n" +
                "             WHERE PAL_ORDINAL = MIN_PAL_ORDINAL\n" +
                "         ),\n" +
                " \n" +
                "     EMAIL AS\n" +
                "         (\n" +
                "             SELECT person.SERIAL                     AS PERSON_SERIAL,\n" +
                "                    COALESCE(personContact.VALUE, '') AS PERSON_EMAIL\n" +
                "             FROM CORE.PERSON AS person\n" +
                "                      LEFT OUTER JOIN\n" +
                "                  (\n" +
                "                      SELECT person.SERIAL              AS PERSON_SERIAL,\n" +
                "                             MIN(personContact.ORDINAL) AS MIN_ORDINAL\n" +
                "                      FROM CORE.PERSON AS person\n" +
                "                               INNER JOIN CORE.PERSON_CONTACT AS personContact\n" +
                "                                          ON personContact.PARENT_SERIAL = person.SERIAL\n" +
                "                      WHERE personContact.CATEGORY IN ('PE', 'BE')\n" +
                "                        AND personContact.BAD_CONTACT = 'N'\n" +
                "                        AND personContact.EXPIRATION_DATE IS NULL\n" +
                "                      GROUP BY person.SERIAL\n" +
                "                  ) AS personEmail\n" +
                "                  ON person.SERIAL = personEmail.PERSON_SERIAL\n" +
                "                      LEFT OUTER JOIN CORE.PERSON_CONTACT AS personContact\n" +
                "                                      ON personContact.PARENT_SERIAL = person.SERIAL\n" +
                "                                          AND personContact.ORDINAL = personEmail.MIN_ORDINAL\n" +
                "         ),\n" +
                " \n" +
                "     PHONE_NUMBER AS (\n" +
                "         SELECT person.SERIAL                     AS PERSON_SERIAL,\n" +
                "                COALESCE(personContact.VALUE, '') AS PERSON_PHONE\n" +
                "         FROM CORE.PERSON AS person\n" +
                "                  LEFT OUTER JOIN\n" +
                "              (\n" +
                "                  SELECT person.SERIAL              AS PERSON_SERIAL,\n" +
                "                         MIN(personContact.ORDINAL) AS MIN_ORDINAL\n" +
                "                  FROM CORE.PERSON AS person\n" +
                "                           INNER JOIN CORE.PERSON_CONTACT AS personContact\n" +
                "                                      ON personContact.PARENT_SERIAL = person.SERIAL\n" +
                "                  WHERE personContact.CATEGORY IN ('HP', 'BP', 'PC', 'BC')\n" +
                "                    AND personContact.BAD_CONTACT = 'N'\n" +
                "                    AND personContact.EXPIRATION_DATE IS NULL\n" +
                "                  GROUP BY person.SERIAL\n" +
                "              ) AS personPhone\n" +
                "              ON person.SERIAL = personPhone.PERSON_SERIAL\n" +
                "                  LEFT OUTER JOIN CORE.PERSON_CONTACT AS personContact\n" +
                "                                  ON personContact.PARENT_SERIAL = person.SERIAL\n" +
                "                                      AND personContact.ORDINAL = personPhone.MIN_ORDINAL\n" +
                "     )\n" +
                " \n" +
                " SELECT MEMBERSHIP.LAST_NAME || COALESCE(', ' || MEMBERSHIP.FIRST_NAME, '') || COALESCE(' ' || MEMBERSHIP.MIDDLE_NAME, '') MemberName,\n" +
                "       MEMBERSHIP.ACCOUNT_NUMBER,\n" +
                "       MEMBERSHIP.DOB,\n" +
                "       COALESCE(YEAR(current_date - MEMBERSHIP.DOB), 0) AGE,\n" +
                "       CurrentAddress.combinedAddress,\n" +
                "       EMAIL.PERSON_EMAIL,\n" +
                "       PHONE_NUMBER.PERSON_PHONE,  \n" +
                "       MEMBERSHIP.OPEN_DATE,\n" +
                "       MEMBERSHIP.person_serial\n" +
                " FROM MEMBERSHIP INNER JOIN CurrentAddress ON MEMBERSHIP.person_serial = CurrentAddress.personSerial\n" +
                " LEFT JOIN EMAIL ON MEMBERSHIP.person_serial = EMAIL.PERSON_SERIAL\n" +
                " LEFT JOIN PHONE_NUMBER ON MEMBERSHIP.person_serial = PHONE_NUMBER.PERSON_SERIAL\n" +
                " WHERE COALESCE(YEAR(current_date - MEMBERSHIP.DOB), 0) > 18 OR COALESCE(YEAR(current_date - MEMBERSHIP.DOB), 0) = 0\n" +
                " \n";

    }
}
