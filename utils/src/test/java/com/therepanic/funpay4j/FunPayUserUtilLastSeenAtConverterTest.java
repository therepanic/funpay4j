/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.therepanic.funpay4j;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * @author therepanic
 * @since 1.0.2
 */
class FunPayUserUtilLastSeenAtConverterTest {
    @Test
    void testConvertLastSeenAtStringToDateToday() throws Exception {
        String input = "Был сегодня в 12:30 (2 часа назад)";
        Date result = FunPayUserUtil.convertLastSeenAtStringToDate(input);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date expectedDate = calendar.getTime();

        assertEquals(expectedDate, result);
    }

    @Test
    void testConvertLastSeenAtStringToDateYesterday() throws Exception {
        String input = "Был вчера в 23:15 (1 день назад)";
        Date result = FunPayUserUtil.convertLastSeenAtStringToDate(input);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 15);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date expectedDate = calendar.getTime();

        assertEquals(expectedDate, result);
    }

    @Test
    void testConvertLastSeenAtStringToDateWithoutYear() throws Exception {
        String input = "Был 5 октября в 19:45 (1 неделя назад)";
        Date result = FunPayUserUtil.convertLastSeenAtStringToDate(input);

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.MONTH, Calendar.OCTOBER);
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 45);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Date expectedDate = calendar.getTime();

        assertEquals(expectedDate, result);
    }

    @Test
    void testConvertLastSeenAtStringToDateWithYear() throws Exception {
        String input = "Был 11 июля 2019 в 15:52 (5 лет назад)";
        Date result = FunPayUserUtil.convertLastSeenAtStringToDate(input);

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("d MMMM yyyy 'в' HH:mm", Locale.forLanguageTag("ru"));
        Date expectedDate = dateFormat.parse("11 июля 2019 в 15:52");

        assertEquals(expectedDate, result);
    }
}
