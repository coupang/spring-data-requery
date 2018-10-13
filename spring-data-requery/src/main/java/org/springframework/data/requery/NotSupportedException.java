/*
 * Copyright 2018 Coupang Ltd.
 *
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

package org.springframework.data.requery;

/**
 * 지원하지 않는 기능을 사용하려고 할 때 발생하는 exception.
 *
 * @author debop
 * @since 18. 6. 22
 */
public class NotSupportedException extends RuntimeException {

    public NotSupportedException() {}

    public NotSupportedException(final String message) {
        super(message);
    }

    public NotSupportedException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NotSupportedException(final Throwable cause) {
        super(cause);
    }

    private static final long serialVersionUID = -4471049196596338256L;
}
