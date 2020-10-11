//package com.easy.kotlins.helper
//
//import android.graphics.Bitmap
//import android.graphics.drawable.Drawable
//import android.net.Uri
//import android.widget.ImageView
//import androidx.annotation.DrawableRes
//import java.io.File
//
///**
// * Create by LiZhanPing on 2020/8/27
// */
//
//var ImageView?.imageUrl: String?
//    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("no getter")
//    set(value) {
//        this?.let { ImageLoader.load(value).into(it) }
//    }
//
//var ImageView?.imageUri: Uri?
//    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("no getter")
//    set(value) {
//        this?.let { ImageLoader.load(value).into(it) }
//    }
//
//var ImageView?.imageResource: Int
//    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("no getter")
//    set(value) {
//        this?.let { ImageLoader.load(value).into(it) }
//    }
//
//var ImageView?.imageDrawable: Drawable?
//    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("no getter")
//    set(value) {
//        this?.setImageDrawable(value)
//    }
//
//var ImageView?.imageBitmap: Bitmap?
//    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("no getter")
//    set(value) {
//        this?.setImageBitmap(value)
//    }
//
//var ImageView?.imageFile: File?
//    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("no getter")
//    set(value) {
//        this?.let { ImageLoader.load(value).into(it) }
//    }
//
//var ImageView?.imageModel: ImageModel<*>
//    @Deprecated("NO_GETTER", level = DeprecationLevel.ERROR) get() = error("no getter")
//    set(value) {
//        this?.run {
//            when (value) {
//                is UrlImageModel -> ImageLoader.load(value.source).placeholder(value.placeholderDrawable).error(value.errorDrawable).into(this)
//                is UriImageModel -> ImageLoader.load(value.source).placeholder(value.placeholderDrawable).error(value.errorDrawable).into(this)
//                is FileImageModel -> ImageLoader.load(value.source).placeholder(value.placeholderDrawable).error(value.errorDrawable).into(this)
//                else -> error("unsupported image model " + value.javaClass.simpleName)
//            }
//        }
//    }
//
//fun ImageView?.fit() = run { this?.scaleType = ImageView.ScaleType.FIT_XY }
//
//fun ImageView?.fitCenter() = run { this?.scaleType = ImageView.ScaleType.FIT_CENTER }
//
//fun ImageView?.center() = run { this?.scaleType = ImageView.ScaleType.CENTER }
//
//fun ImageView?.centerInside() = run { this?.scaleType = ImageView.ScaleType.CENTER_INSIDE }
//
//fun ImageView?.centerCrop() = run { this?.scaleType = ImageView.ScaleType.CENTER_CROP }
//
//abstract class ImageModel<T> {
//
//    val source: T
//    val placeholderDrawable: Drawable?
//    val errorDrawable: Drawable?
//
//    constructor(source: T) : this(source, null, null)
//
//    constructor(source: T, placeholderDrawable: Drawable?, errorDrawable: Drawable?) {
//        this.source = source
//        this.placeholderDrawable = placeholderDrawable
//        this.errorDrawable = errorDrawable
//    }
//
//    constructor(source: T, @DrawableRes placeholderResource: Int, errorResource: Int) {
//        this.source = source
//        this.placeholderDrawable = App.getContext().getCompatDrawable(placeholderResource)
//        this.errorDrawable = App.getContext().getCompatDrawable(errorResource)
//    }
//}
//
//class UrlImageModel : ImageModel<String> {
//    constructor(source: String) : super(source)
//    constructor(source: String, placeholderDrawable: Drawable?, errorDrawable: Drawable?) : super(source, placeholderDrawable, errorDrawable)
//    constructor(source: String, placeholderResource: Int, errorResource: Int) : super(source, placeholderResource, errorResource)
//}
//
//class UriImageModel : ImageModel<Uri> {
//    constructor(source: Uri) : super(source)
//    constructor(source: Uri, placeholderDrawable: Drawable?, errorDrawable: Drawable?) : super(source, placeholderDrawable, errorDrawable)
//    constructor(source: Uri, placeholderResource: Int, errorResource: Int) : super(source, placeholderResource, errorResource)
//}
//
//class FileImageModel : ImageModel<File> {
//    constructor(source: File) : super(source)
//    constructor(source: File, placeholderDrawable: Drawable?, errorDrawable: Drawable?) : super(source, placeholderDrawable, errorDrawable)
//    constructor(source: File, placeholderResource: Int, errorResource: Int) : super(source, placeholderResource, errorResource)
//}
//
//fun urlImageModel(source: String, placeholderDrawable: Drawable? = null, errorDrawable: Drawable? = null): UrlImageModel = UrlImageModel(source, placeholderDrawable, errorDrawable)
//
//fun urlImageModel(source: String, placeholderResource: Int = 0, errorResource: Int = 0): UrlImageModel = UrlImageModel(source, placeholderResource, errorResource)
//
//fun uriImageModel(source: Uri, placeholderDrawable: Drawable? = null, errorDrawable: Drawable? = null): UriImageModel = UriImageModel(source, placeholderDrawable, errorDrawable)
//
//fun uriImageModel(source: Uri, placeholderResource: Int = 0, errorResource: Int = 0): UriImageModel = UriImageModel(source, placeholderResource, errorResource)
//
//fun fileImageModel(source: File, placeholderDrawable: Drawable? = null, errorDrawable: Drawable? = null): FileImageModel = FileImageModel(source, placeholderDrawable, errorDrawable)
//
//fun fileImageModel(source: File, placeholderResource: Int = 0, errorResource: Int = 0): FileImageModel = FileImageModel(source, placeholderResource, errorResource)
