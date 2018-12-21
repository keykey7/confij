package ch.kk7.confij.common;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.types.ResolvedObjectType;
import lombok.experimental.UtilityClass;

import java.util.function.Predicate;

@UtilityClass
public class Util {
	public static final ResolvedType rawObjectType = ResolvedObjectType.create(Object.class, null, null, null);

	public static <T> Predicate<T> not(Predicate<T> t) {
		return t.negate();
	}
}
