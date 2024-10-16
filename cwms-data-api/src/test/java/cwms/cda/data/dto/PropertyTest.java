/*
 * MIT License
 *
 * Copyright (c) 2024 Hydrologic Engineering Center
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cwms.cda.data.dto;

import cwms.cda.api.errors.FieldException;
import cwms.cda.formatters.ContentType;
import cwms.cda.formatters.Formats;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

final class PropertyTest {

    @Test
    void createProperty_allFieldsProvided_success() {
        Property item = new Property.Builder()
                .withCategory("TestCategory")
                .withOfficeId("TestOffice")
                .withName("TestName")
                .withValue("TestValue")
                .build();
        assertAll(() -> assertEquals("TestCategory", item.getCategory(), "The category does not match the provided value"),
                () -> assertEquals("TestOffice", item.getOfficeId(), "The office does not match the provided value"),
                () -> assertEquals("TestName", item.getName(), "The name does not match the provided value"),
                () -> assertEquals("TestValue", item.getValue(), "The value does not match the provided value"));
    }

    @Test
    void createProperty_missingField_throwsFieldException() {
        assertAll(
                // When Office is missing
                () -> assertThrows(FieldException.class, () -> {
                    Property item = new Property.Builder()
                            .withCategory("TestCategory")
                            // missing Office
                            .withName("TestName")
                            .withValue("TestValue")
                            .build();
                    item.validate();
                }, "The validate method should have thrown a FieldException because the office field is missing"),

                // When Category is missing
                () -> assertThrows(FieldException.class, () -> {
                    Property item = new Property.Builder()
                            // missing Category
                            .withOfficeId("TestOffice")
                            .withName("TestName")
                            .withValue("TestValue")
                            .build();
                    item.validate();
                }, "The validate method should have thrown a FieldException because the category field is missing"),

                // When Name is missing
                () -> assertThrows(FieldException.class, () -> {
                    Property item = new Property.Builder()
                            .withCategory("TestCategory")
                            .withOfficeId("TestOffice")
                            // missing Name
                            .withValue("TestValue")
                            .build();
                    item.validate();
                }));
    }

    @Test
    void createProperty_serialize_roundtrip() throws Exception {
        Property property = new Property.Builder()
                .withCategory("TestCategory")
                .withOfficeId("TestOffice")
                .withName("TestName")
                .withValue("TestValue")
                .withComment("TestComment")
                .build();
        ContentType contentType = new ContentType(Formats.JSON);
        String json = Formats.format(contentType, property);
        Property deserialized = Formats.parseContent(contentType, json, Property.class);
        assertEquals(property, deserialized, "Property deserialized from JSON doesn't equal original");
    }

    @Test
    void createProperty_deserialize() throws Exception {
        Property property = new Property.Builder()
                .withCategory("TestCategory")
                .withOfficeId("TestOffice")
                .withName("TestName")
                .withValue("TestValue")
                .withComment("TestComment")
                .build();
        InputStream resource = this.getClass().getResourceAsStream("/cwms/cda/data/dto/property.json");
        assertNotNull(resource);
        String json = IOUtils.toString(resource, StandardCharsets.UTF_8);
        ContentType contentType = new ContentType(Formats.JSON);
        Property deserialized = Formats.parseContent(contentType, json, Property.class);
        assertEquals(property, deserialized, "Property deserialized from JSON doesn't equal original");
    }
}
