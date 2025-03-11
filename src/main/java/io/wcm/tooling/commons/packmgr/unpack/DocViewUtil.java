/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2025 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.tooling.commons.packmgr.unpack;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Binary;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.vault.util.DocViewProperty2;

final class DocViewUtil {

  private DocViewUtil() {
    // static methods only
  }

  /**
   * Parses a multi-value property value in DocView format.
   * @param value Multi-valued property
   * @return Property values
   */
  static List<String> parseValues(String value) {
    try {
      DocViewProperty2 prop = DocViewProperty2.parse(DUMMY_NAME, value);
      return prop.getStringValues();
    }
    catch (RepositoryException ex) {
      throw new IllegalArgumentException("Unable to parse values: " + value, ex);
    }
  }

  /**
   * Formats multiple values on DocView as a single string.
   * @param values Values
   * @return DocView values string
   */
  static String formatValues(List<String> values) {
    return formatValues(values, PropertyType.STRING);
  }

  /**
   * Formats multiple values on DocView as a single string.
   * @param values Values
   * @param propertyType Property type from {@link PropertyType}
   * @return DocView values string
   */
  static String formatValues(List<String> values, int propertyType) {
    Value[] valueObjects = values.stream()
        .map(value -> new MockValue(value, propertyType))
        .toArray(size -> new Value[size]);
    try {
      return DocViewProperty2.fromValues(DUMMY_NAME, valueObjects, propertyType, true, false, false).formatValue();
    }
    catch (RepositoryException ex) {
      throw new IllegalArgumentException("Unable to format values: " + valueObjects, ex);
    }
  }


  private static final Name DUMMY_NAME = new Name() {
    private static final long serialVersionUID = 1L;

    @Override
    public String getLocalName() {
      return "dummy";
    }

    @Override
    public String getNamespaceURI() {
      return NS_DEFAULT_URI;
    }

    @Override
    public int compareTo(Object o) {
      throw new UnsupportedOperationException();
    }
  };

  private static class MockValue implements Value {

    private final String value;
    private final int type;

    MockValue(String value, int type) {
      this.value = value;
      this.type = type;
    }

    @Override
    public String getString() {
      return value;
    }

    @Override
    public int getType() {
      return type;
    }


    // -- unsupported methods --

    @Override
    public InputStream getStream() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Binary getBinary() {
      throw new UnsupportedOperationException();
    }

    @Override
    public long getLong() {
      throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble() {
      throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getDecimal() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Calendar getDate() {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean() {
      throw new UnsupportedOperationException();
    }

  }

}
