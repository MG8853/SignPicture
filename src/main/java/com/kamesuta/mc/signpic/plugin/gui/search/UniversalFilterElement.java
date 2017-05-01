package com.kamesuta.mc.signpic.plugin.gui.search;

import javax.annotation.Nullable;

import com.kamesuta.mc.signpic.plugin.SignData;

public abstract class UniversalFilterElement implements IFilterElement {
	public final @Nullable String str;

	public UniversalFilterElement(final String src) {
		this.str = src;
	}

	@Override
	public boolean filter(final SignData data) {
		for (final StringFilterProperty line : StringFilterProperty.values())
			if (propFilter(data, line))
				return true;
		return false;
	}

	protected abstract boolean propFilter(SignData data, StringFilterProperty prop);

	public static class EqualsUniversalFilterElement extends UniversalFilterElement {

		public EqualsUniversalFilterElement(final String src) {
			super(src);
		}

		@Override
		protected boolean propFilter(final SignData data, final StringFilterProperty prop) {
			return new StringFilterElement.EqualsStringFilterElement(prop, this.str).filter(data);
		}

	}

	public static class EqualsIgnoreCaseUniversalFilterElement extends UniversalFilterElement {

		public EqualsIgnoreCaseUniversalFilterElement(final String src) {
			super(src);
		}

		@Override
		protected boolean propFilter(final SignData data, final StringFilterProperty prop) {
			return new StringFilterElement.EqualsIgnoreCaseStringFilterElement(prop, this.str).filter(data);
		}
	}

	public static class ContainsUniversalFilterElement extends UniversalFilterElement {

		public ContainsUniversalFilterElement(final String src) {
			super(src);
		}

		@Override
		protected boolean propFilter(final SignData data, final StringFilterProperty prop) {
			return new StringFilterElement.ContainsStringFilterElement(prop, this.str).filter(data);
		}

	}

	public static class ContainsIgnoreCaseUniversalFilterElement extends UniversalFilterElement {

		public ContainsIgnoreCaseUniversalFilterElement(final String src) {
			super(src);
		}

		@Override
		protected boolean propFilter(final SignData data, final StringFilterProperty prop) {
			return new StringFilterElement.ContainsIgnoreCaseStringFilterElement(prop, this.str).filter(data);
		}

	}
}
