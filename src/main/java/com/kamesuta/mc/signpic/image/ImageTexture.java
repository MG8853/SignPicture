package com.kamesuta.mc.signpic.image;

import com.kamesuta.mc.signpic.image.meta.SizeData;

public interface ImageTexture {

	void bind();

	SizeData getSize();

	boolean hasMipmap();

}