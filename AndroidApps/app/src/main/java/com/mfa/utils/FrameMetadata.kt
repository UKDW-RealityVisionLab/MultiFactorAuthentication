package com.mfa.utils

/** Describing a frame info. */
class FrameMetadata(width: Int, height: Int, rotation: Int) {

    private var width = 0
    private var height = 0
    private var rotation = 0

    init {
        this.width = width
        this.height = height
        this.rotation = rotation
    }

    fun getWidth(): Int {
        return width
    }

    fun getHeight(): Int {
        return height
    }

    fun getRotation(): Int {
        return rotation
    }

    /** Builder of [FrameMetadata].  */
    class Builder {
        private var width = 0
        private var height = 0
        private var rotation = 0

        fun setWidth(width: Int): Builder {
            this.width = width
            return this
        }

        fun setHeight(height: Int): Builder {
            this.height = height
            return this
        }

        fun setRotation(rotation: Int): Builder {
            this.rotation = rotation
            return this
        }

        fun build(): FrameMetadata {
            return FrameMetadata(width, height, rotation)
        }
    }
}