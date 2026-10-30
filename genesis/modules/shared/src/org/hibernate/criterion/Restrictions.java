/*
 * The Genesis Project
 * Copyright (C) 2004-2026  Summa Technologies do Brasil Ltda.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.hibernate.criterion;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class Restrictions {
   private Restrictions() {
   }

   static Path resolvePath(final Path<?> root, final String propertyName) {
      Path current = root;
      final String[] parts = propertyName.split("\\.");
      for (int i = 0; i < parts.length; i++) {
         current = current.get(parts[i]);
      }
      return current;
   }

   public static Criterion eq(final String propertyName, final Object value) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            final Path path = resolvePath(root, propertyName);
            return value == null ? cb.isNull(path) : cb.equal(path, value);
         }
      };
   }

   public static Criterion ne(final String propertyName, final Object value) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            final Path path = resolvePath(root, propertyName);
            return value == null ? cb.isNotNull(path) : cb.notEqual(path, value);
         }
      };
   }

   public static Criterion like(final String propertyName, final Object value) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            final Expression<String> path = resolvePath(root, propertyName);
            return cb.like(path, String.valueOf(value));
         }
      };
   }

   public static Criterion like(
         final String propertyName, final String value, final MatchMode matchMode) {
      return like(propertyName, matchMode.toMatchString(value));
   }

   public static Criterion ilike(final String propertyName, final Object value) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            final Expression<String> path = resolvePath(root, propertyName);
            return cb.like(cb.lower(path), String.valueOf(value).toLowerCase(Locale.ROOT));
         }
      };
   }

   public static Criterion ilike(
         final String propertyName, final String value, final MatchMode matchMode) {
      return ilike(propertyName, matchMode.toMatchString(value));
   }

   public static Criterion gt(final String propertyName, final Object value) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            final Expression<Comparable> path = resolvePath(root, propertyName);
            return cb.greaterThan(path, (Comparable) value);
         }
      };
   }

   public static Criterion lt(final String propertyName, final Object value) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            final Expression<Comparable> path = resolvePath(root, propertyName);
            return cb.lessThan(path, (Comparable) value);
         }
      };
   }

   public static Criterion le(final String propertyName, final Object value) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            final Expression<Comparable> path = resolvePath(root, propertyName);
            return cb.lessThanOrEqualTo(path, (Comparable) value);
         }
      };
   }

   public static Criterion ge(final String propertyName, final Object value) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            final Expression<Comparable> path = resolvePath(root, propertyName);
            return cb.greaterThanOrEqualTo(path, (Comparable) value);
         }
      };
   }

   public static Criterion between(
         final String propertyName, final Object lo, final Object hi) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            final Expression<Comparable> path = resolvePath(root, propertyName);
            return cb.between(path, (Comparable) lo, (Comparable) hi);
         }
      };
   }

   public static Criterion in(final String propertyName, final Object[] values) {
      return in(propertyName, Arrays.asList(values));
   }

   public static Criterion in(final String propertyName, final Collection values) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            final Path path = resolvePath(root, propertyName);
            return path.in(values);
         }
      };
   }

   public static Criterion isNull(final String propertyName) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            return cb.isNull(resolvePath(root, propertyName));
         }
      };
   }

   public static Criterion isNotNull(final String propertyName) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            return cb.isNotNull(resolvePath(root, propertyName));
         }
      };
   }

   public static Criterion and(final Criterion lhs, final Criterion rhs) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            return cb.and(
                  lhs.toPredicate(root, query, cb),
                  rhs.toPredicate(root, query, cb));
         }
      };
   }

   public static Criterion or(final Criterion lhs, final Criterion rhs) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            return cb.or(
                  lhs.toPredicate(root, query, cb),
                  rhs.toPredicate(root, query, cb));
         }
      };
   }

   public static Criterion not(final Criterion expression) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            return cb.not(expression.toPredicate(root, query, cb));
         }
      };
   }

   public static Criterion allEq(final Map propertyNameValues) {
      return new Criterion() {
         private static final long serialVersionUID = 1L;

         @Override
         public Predicate toPredicate(Root<?> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            final List<Predicate> list = new ArrayList<Predicate>(propertyNameValues.size());
            for (final Object obj : propertyNameValues.entrySet()) {
               final Map.Entry entry = (Map.Entry) obj;
               list.add(eq(String.valueOf(entry.getKey()), entry.getValue())
                     .toPredicate(root, query, cb));
            }
            return cb.and(list.toArray(new Predicate[list.size()]));
         }
      };
   }
}
