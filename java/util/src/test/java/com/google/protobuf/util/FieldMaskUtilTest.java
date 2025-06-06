// Protocol Buffers - Google's data interchange format
// Copyright 2008 Google Inc.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

package com.google.protobuf.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.FieldMask;
import com.google.protobuf.util.FieldMaskUtil.TrimOptions;
import proto2_unittest.UnittestProto.NestedTestAllTypes;
import proto2_unittest.UnittestProto.TestAllTypes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link FieldMaskUtil}. */
@RunWith(JUnit4.class)
public class FieldMaskUtilTest {
  @Test
  public void testIsValid() throws Exception {
    assertThat(FieldMaskUtil.isValid(NestedTestAllTypes.class, "payload")).isTrue();
    assertThat(FieldMaskUtil.isValid(NestedTestAllTypes.class, "nonexist")).isFalse();
    assertThat(FieldMaskUtil.isValid(NestedTestAllTypes.class, "payload.optional_int32")).isTrue();
    assertThat(FieldMaskUtil.isValid(NestedTestAllTypes.class, "payload.repeated_int32")).isTrue();
    assertThat(FieldMaskUtil.isValid(NestedTestAllTypes.class, "payload.optional_nested_message"))
        .isTrue();
    assertThat(FieldMaskUtil.isValid(NestedTestAllTypes.class, "payload.repeated_nested_message"))
        .isTrue();
    assertThat(FieldMaskUtil.isValid(NestedTestAllTypes.class, "payload.nonexist")).isFalse();

    assertThat(FieldMaskUtil.isValid(NestedTestAllTypes.class, FieldMaskUtil.fromString("payload")))
        .isTrue();
    assertThat(
            FieldMaskUtil.isValid(NestedTestAllTypes.class, FieldMaskUtil.fromString("nonexist")))
        .isFalse();
    assertThat(
            FieldMaskUtil.isValid(
                NestedTestAllTypes.class, FieldMaskUtil.fromString("payload,nonexist")))
        .isFalse();

    assertThat(FieldMaskUtil.isValid(NestedTestAllTypes.getDescriptor(), "payload")).isTrue();
    assertThat(FieldMaskUtil.isValid(NestedTestAllTypes.getDescriptor(), "nonexist")).isFalse();

    assertThat(
            FieldMaskUtil.isValid(
                NestedTestAllTypes.getDescriptor(), FieldMaskUtil.fromString("payload")))
        .isTrue();
    assertThat(
            FieldMaskUtil.isValid(
                NestedTestAllTypes.getDescriptor(), FieldMaskUtil.fromString("nonexist")))
        .isFalse();

    assertThat(
            FieldMaskUtil.isValid(NestedTestAllTypes.class, "payload.optional_nested_message.bb"))
        .isTrue();
    // Repeated fields cannot have sub-paths.
    assertThat(
            FieldMaskUtil.isValid(NestedTestAllTypes.class, "payload.repeated_nested_message.bb"))
        .isFalse();
    // Non-message fields cannot have sub-paths.
    assertThat(FieldMaskUtil.isValid(NestedTestAllTypes.class, "payload.optional_int32.bb"))
        .isFalse();
  }

  @Test
  public void testToString() throws Exception {
    assertThat(FieldMaskUtil.toString(FieldMask.getDefaultInstance())).isEmpty();
    FieldMask mask = FieldMask.newBuilder().addPaths("foo").build();
    assertThat(FieldMaskUtil.toString(mask)).isEqualTo("foo");
    mask = FieldMask.newBuilder().addPaths("foo").addPaths("bar").build();
    assertThat(FieldMaskUtil.toString(mask)).isEqualTo("foo,bar");

    // Empty field paths are ignored.
    mask =
        FieldMask.newBuilder()
            .addPaths("")
            .addPaths("foo")
            .addPaths("")
            .addPaths("bar")
            .addPaths("")
            .build();
    assertThat(FieldMaskUtil.toString(mask)).isEqualTo("foo,bar");
  }

  @Test
  public void testFromString() throws Exception {
    FieldMask mask = FieldMaskUtil.fromString("");
    assertThat(mask).isEqualTo(FieldMask.getDefaultInstance());

    mask = FieldMaskUtil.fromString("foo");
    assertThat(mask).isEqualTo(FieldMask.newBuilder().addPaths("foo").build());

    mask = FieldMaskUtil.fromString("foo,bar.baz");
    assertThat(mask).isEqualTo(FieldMask.newBuilder().addPaths("foo").addPaths("bar.baz").build());

    // Empty field paths are ignore.
    mask = FieldMaskUtil.fromString(",foo,,bar,");
    assertThat(mask).isEqualTo(FieldMask.newBuilder().addPaths("foo").addPaths("bar").build());

    // The field paths are valid if a class parameter is provided.
    mask = FieldMaskUtil.fromString(NestedTestAllTypes.class, ",payload,child");
    assertThat(mask)
        .isEqualTo(FieldMask.newBuilder().addPaths("payload").addPaths("child").build());

    assertThrows(
        IllegalArgumentException.class,
        () -> FieldMaskUtil.fromString(NestedTestAllTypes.class, "payload,nonexist"));
  }

  @Test
  public void testFromFieldNumbers() throws Exception {
    FieldMask mask = FieldMaskUtil.fromFieldNumbers(TestAllTypes.class);
    assertThat(mask).isEqualTo(FieldMask.getDefaultInstance());

    mask =
        FieldMaskUtil.fromFieldNumbers(
            TestAllTypes.class, TestAllTypes.OPTIONAL_INT32_FIELD_NUMBER);
    assertThat(mask).isEqualTo(FieldMask.newBuilder().addPaths("optional_int32").build());

    mask =
        FieldMaskUtil.fromFieldNumbers(
            TestAllTypes.class,
            TestAllTypes.OPTIONAL_INT32_FIELD_NUMBER,
            TestAllTypes.OPTIONAL_INT64_FIELD_NUMBER);
    assertThat(mask)
        .isEqualTo(
            FieldMask.newBuilder().addPaths("optional_int32").addPaths("optional_int64").build());

    assertThrows(
        IllegalArgumentException.class,
        () -> FieldMaskUtil.fromFieldNumbers(TestAllTypes.class, 1000 /* invalid field number */));
  }

  @Test
  public void testToJsonString() throws Exception {
    FieldMask mask = FieldMask.getDefaultInstance();
    assertThat(FieldMaskUtil.toJsonString(mask)).isEmpty();

    mask = FieldMask.newBuilder().addPaths("foo").build();
    assertThat(FieldMaskUtil.toJsonString(mask)).isEqualTo("foo");

    mask = FieldMask.newBuilder().addPaths("foo.bar_baz").addPaths("").build();
    assertThat(FieldMaskUtil.toJsonString(mask)).isEqualTo("foo.barBaz");

    mask = FieldMask.newBuilder().addPaths("foo").addPaths("bar_baz").build();
    assertThat(FieldMaskUtil.toJsonString(mask)).isEqualTo("foo,barBaz");
  }

  @Test
  public void testFromJsonString() throws Exception {
    FieldMask mask = FieldMaskUtil.fromJsonString("");
    assertThat(mask).isEqualTo(FieldMask.getDefaultInstance());

    mask = FieldMaskUtil.fromJsonString("foo");
    assertThat(mask).isEqualTo(FieldMask.newBuilder().addPaths("foo").build());

    mask = FieldMaskUtil.fromJsonString("foo.barBaz");
    assertThat(mask).isEqualTo(FieldMask.newBuilder().addPaths("foo.bar_baz").build());

    mask = FieldMaskUtil.fromJsonString("foo,barBaz");
    assertThat(mask).isEqualTo(FieldMask.newBuilder().addPaths("foo").addPaths("bar_baz").build());
  }

  @Test
  public void testFromStringList() throws Exception {
    FieldMask mask =
        FieldMaskUtil.fromStringList(
            NestedTestAllTypes.class, ImmutableList.of("payload.repeated_nested_message", "child"));
    assertThat(mask)
        .isEqualTo(
            FieldMask.newBuilder()
                .addPaths("payload.repeated_nested_message")
                .addPaths("child")
                .build());

    mask =
        FieldMaskUtil.fromStringList(
            NestedTestAllTypes.getDescriptor(),
            ImmutableList.of("payload.repeated_nested_message", "child"));
    assertThat(mask)
        .isEqualTo(
            FieldMask.newBuilder()
                .addPaths("payload.repeated_nested_message")
                .addPaths("child")
                .build());

    mask =
        FieldMaskUtil.fromStringList(ImmutableList.of("payload.repeated_nested_message", "child"));
    assertThat(mask)
        .isEqualTo(
            FieldMask.newBuilder()
                .addPaths("payload.repeated_nested_message")
                .addPaths("child")
                .build());
  }

  @Test
  public void testUnion() throws Exception {
    // Only test a simple case here and expect
    // {@link FieldMaskTreeTest#testAddFieldPath} to cover all scenarios.
    FieldMask mask1 = FieldMaskUtil.fromString("foo,bar.baz,bar.quz");
    FieldMask mask2 = FieldMaskUtil.fromString("foo.bar,bar");

    FieldMask result = FieldMaskUtil.union(mask1, mask2);

    assertThat(FieldMaskUtil.toString(result)).isEqualTo("bar,foo");
  }

  @Test
  public void testUnion_usingVarArgs() throws Exception {
    FieldMask mask1 = FieldMaskUtil.fromString("foo");
    FieldMask mask2 = FieldMaskUtil.fromString("foo.bar,bar.quz");
    FieldMask mask3 = FieldMaskUtil.fromString("bar.quz");
    FieldMask mask4 = FieldMaskUtil.fromString("bar");

    FieldMask result = FieldMaskUtil.union(mask1, mask2, mask3, mask4);

    assertThat(FieldMaskUtil.toString(result)).isEqualTo("bar,foo");
  }

  @Test
  public void testSubtract() throws Exception {
    // Only test a simple case here and expect
    // {@link FieldMaskTreeTest#testRemoveFieldPath} to cover all scenarios.
    FieldMask mask1 = FieldMaskUtil.fromString("foo,bar.baz,bar.quz");
    FieldMask mask2 = FieldMaskUtil.fromString("foo.bar,bar");

    FieldMask result = FieldMaskUtil.subtract(mask1, mask2);

    assertThat(FieldMaskUtil.toString(result)).isEqualTo("foo");
  }

  @Test
  public void testSubtract_usingVarArgs() throws Exception {
    FieldMask mask1 = FieldMaskUtil.fromString("foo,bar.baz,bar.quz.bar");
    FieldMask mask2 = FieldMaskUtil.fromString("foo.bar,bar.baz.quz");
    FieldMask mask3 = FieldMaskUtil.fromString("bar.quz");
    FieldMask mask4 = FieldMaskUtil.fromString("foo,bar.baz");

    FieldMask result = FieldMaskUtil.subtract(mask1, mask2, mask3, mask4);

    assertThat(FieldMaskUtil.toString(result)).isEmpty();
  }

  @Test
  public void testIntersection() throws Exception {
    // Only test a simple case here and expect
    // {@link FieldMaskTreeTest#testIntersectFieldPath} to cover all scenarios.
    FieldMask mask1 = FieldMaskUtil.fromString("foo,bar.baz,bar.quz");
    FieldMask mask2 = FieldMaskUtil.fromString("foo.bar,bar");

    FieldMask result = FieldMaskUtil.intersection(mask1, mask2);

    assertThat(FieldMaskUtil.toString(result)).isEqualTo("bar.baz,bar.quz,foo.bar");
  }

  @Test
  public void testMerge() throws Exception {
    // Only test a simple case here and expect
    // {@link FieldMaskTreeTest#testMerge} to cover all scenarios.
    NestedTestAllTypes source =
        NestedTestAllTypes.newBuilder()
            .setPayload(TestAllTypes.newBuilder().setOptionalInt32(1234))
            .build();
    NestedTestAllTypes.Builder builder = NestedTestAllTypes.newBuilder();

    FieldMaskUtil.merge(FieldMaskUtil.fromString("payload"), source, builder);

    assertThat(builder.getPayload().getOptionalInt32()).isEqualTo(1234);
  }

  @Test
  public void testTrim() throws Exception {
    NestedTestAllTypes source =
        NestedTestAllTypes.newBuilder()
            .setPayload(
                TestAllTypes.newBuilder()
                    .setOptionalInt32(1234)
                    .setOptionalString("1234")
                    .setOptionalBool(true))
            .build();
    FieldMask mask =
        FieldMaskUtil.fromStringList(
            ImmutableList.of(
                "payload.optional_int32",
                "payload.optional_string",
                "payload.optional_int64", // Primitive field not set in source.
                "child", // Message field not set in source.
                "child.optional_int32" // Primitive field in an unset message field in source.
                ));

    NestedTestAllTypes actual = FieldMaskUtil.trim(mask, source);

    assertThat(actual)
        .isEqualTo(
            NestedTestAllTypes.newBuilder()
                .setPayload(
                    TestAllTypes.newBuilder()
                        .setOptionalInt32(1234)
                        .setOptionalInt64(0) // Default value set for primitive field.
                        .setOptionalString("1234"))
                .build());
  }

  @Test
  public void testTrimWithTrimOptions_default() throws Exception {
    NestedTestAllTypes source =
        NestedTestAllTypes.newBuilder()
            .setPayload(
                TestAllTypes.newBuilder()
                    .setOptionalInt32(1234)
                    .setOptionalString("1234")
                    .setOptionalBool(true))
            .build();
    FieldMask mask =
        FieldMaskUtil.fromStringList(
            ImmutableList.of(
                "payload.optional_int32",
                "payload.optional_string",
                "payload.optional_int64", // Primitive field not set in source.
                "child", // Message field not set in source.
                "child.optional_int32" // Primitive field in an unset message field in source.
                ));

    NestedTestAllTypes actual = FieldMaskUtil.trim(mask, source, new TrimOptions());

    assertThat(actual)
        .isEqualTo(
            NestedTestAllTypes.newBuilder()
                .setPayload(
                    TestAllTypes.newBuilder()
                        .setOptionalInt32(1234)
                        .setOptionalInt64(0) // Default value set for primitive field.
                        .setOptionalString("1234"))
                .build());
  }

  @Test
  public void testTrimWithTrimOptions_retainPrimitiveFieldUnsetState() throws Exception {
    NestedTestAllTypes source =
        NestedTestAllTypes.newBuilder()
            .setPayload(
                TestAllTypes.newBuilder()
                    .setOptionalInt32(1234)
                    .setOptionalString("1234")
                    .setOptionalBool(true))
            .build();
    FieldMask mask =
        FieldMaskUtil.fromStringList(
            ImmutableList.of(
                "payload.optional_int32",
                "payload.optional_string",
                "payload.optional_int64", // Primitive field not set in source.
                "child", // Message field not set in source.
                "child.optional_int32" // Primitive field in an unset message field in source.
                ));

    NestedTestAllTypes actual =
        FieldMaskUtil.trim(mask, source, new TrimOptions().setRetainPrimitiveFieldUnsetState(true));

    assertThat(actual)
        .isEqualTo(
            NestedTestAllTypes.newBuilder()
                .setPayload(
                    TestAllTypes.newBuilder().setOptionalInt32(1234).setOptionalString("1234"))
                .build());
  }
}
