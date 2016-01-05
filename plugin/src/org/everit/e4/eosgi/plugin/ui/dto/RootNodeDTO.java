/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.e4.eosgi.plugin.ui.dto;

import java.util.Observable;

import org.everit.e4.eosgi.plugin.core.EOSGiContext;

public class RootNodeDTO {
  public EOSGiContext context;

  public Observable observable;

  public RootNodeDTO context(final EOSGiContext eosGiContext) {
    context = eosGiContext;
    return this;
  }

  public RootNodeDTO observable(final Observable observable) {
    this.observable = observable;
    return this;
  }

  @Override
  public String toString() {
    return "RootNodeDTO [context=" + context + ", observable=" + observable + "]";
  }

}