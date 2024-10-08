// Protocol Buffers - Google's data interchange format
// Copyright 2024 Google LLC.  All rights reserved.
//
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file or at
// https://developers.google.com/open-source/licenses/bsd

#ifndef GOOGLE_PROTOBUF_HPB_TEMPLATE_HELP_H__
#define GOOGLE_PROTOBUF_HPB_TEMPLATE_HELP_H__

#include <type_traits>

#include "google/protobuf/hpb/ptr.h"

namespace hpb {
namespace internal {

template <typename T>
struct RemovePtr;

template <typename T>
struct RemovePtr<Ptr<T>> {
  using type = T;
};

template <typename T>
struct RemovePtr<T*> {
  using type = T;
};

template <typename T>
using RemovePtrT = typename RemovePtr<T>::type;

template <typename T, typename U = RemovePtrT<T>,
          typename = std::enable_if_t<!std::is_const_v<U>>>
using PtrOrRaw = T;

template <typename T>
using EnableIfHpbClass = std::enable_if_t<
    std::is_base_of<typename T::Access, T>::value &&
    std::is_base_of<typename T::Access, typename T::ExtendableType>::value>;

template <typename T>
using EnableIfMutableProto = std::enable_if_t<!std::is_const<T>::value>;

}  // namespace internal
}  // namespace hpb

#endif  // GOOGLE_PROTOBUF_HPB_TEMPLATE_HELP_H__
