package com.juliusbaer.premarket.utils

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import java.util.*

open class ArrayAdapterNormalized<T> : BaseAdapter, Filterable {
    /**
     * Lock used to modify the content of [.mObjects]. Any write operation
     * performed on the array should be synchronized on this lock. This lock is also
     * used by the filter (see [.getFilter] to make a synchronized copy of
     * the original array of data.
     */
    private val mLock = Any()

    private val mInflater: LayoutInflater

    /**
     * Returns the context associated with this array adapter. The context is used
     * to create views from the resource passed to the constructor.
     *
     * @return The Context associated with this adapter.
     */
    val context: Context

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter.
     */
    private val mResource: Int

    /**
     * The resource indicating what views to inflate to display the content of this
     * array adapter in a drop down widget.
     */
    private var mDropDownResource: Int = 0

    /**
     * Contains the list of objects that represent the data of this ArrayAdapter.
     * The content of this list is referred to as "the array" in the documentation.
     */
    private var mObjects: MutableList<T>? = null

    /**
     * Indicates whether the contents of [.mObjects] came from static resources.
     */
    private var mObjectsFromResources: Boolean = false

    /**
     * If the inflated resource is not a TextView, `mFieldId` is used to find
     * a TextView inside the inflated views hierarchy. This field must contain the
     * identifier that matches the one defined in the resource file.
     */
    private var mFieldId = 0

    /**
     * Indicates whether or not [.notifyDataSetChanged] must be called whenever
     * [.mObjects] is modified.
     */
    private var mNotifyOnChange = true

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    private var mOriginalValues: ArrayList<T>? = null
    private var mFilter: ArrayFilter? = null

    /** Layout inflater used for [.getDropDownView].  */
    private var mDropDownInflater: LayoutInflater? = null

    protected open val emptyItem: T? = null
    var constraint: CharSequence? = null
        private set

    /**
     * Constructor. This constructor will result in the underlying data collection being
     * immutable, so methods such as [.clear] will throw an exception.
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     * instantiating views.
     * @param objects The objects to represent in the ListView.
     */
    constructor(context: Context, @LayoutRes resource: Int, objects: Array<T>) : this(context, resource, 0, listOf<T>(*objects))

    /**
     * Constructor. This constructor will result in the underlying data collection being
     * immutable, so methods such as [.clear] will throw an exception.
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a layout to use when
     * instantiating views.
     * @param textViewResourceId The id of the TextView within the layout resource to be populated
     * @param objects The objects to represent in the ListView.
     */
    constructor(context: Context, @LayoutRes resource: Int,
                @IdRes textViewResourceId: Int, objects: Array<T>) : this(context, resource, textViewResourceId, listOf<T>(*objects))

    /**
     * Constructor
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     * instantiating views.
     * @param objects The objects to represent in the ListView.
     */
    constructor(context: Context, @LayoutRes resource: Int,
                objects: List<T>) : this(context, resource, 0, objects) {
    }

    /**
     * Constructor
     *
     * @param context The current context.
     * @param resource The resource ID for a layout file containing a layout to use when
     * instantiating views.
     * @param textViewResourceId The id of the TextView within the layout resource to be populated
     * @param objects The objects to represent in the ListView.
     */
    @JvmOverloads
    constructor(context: Context, @LayoutRes resource: Int,
                @IdRes textViewResourceId: Int = 0, objects: List<T> = ArrayList()) : this(context, resource, textViewResourceId, objects, false)

    private constructor(context: Context, @LayoutRes resource: Int,
                        @IdRes textViewResourceId: Int, objects: List<T>, objsFromResources: Boolean) {
        this.context = context
        mInflater = LayoutInflater.from(context)
        mDropDownResource = resource
        mResource = mDropDownResource
        mObjects = objects.toMutableList()
        mObjectsFromResources = objsFromResources
        mFieldId = textViewResourceId
    }

    /**
     * Adds the specified object at the end of the array.
     *
     * @param object The object to add at the end of the array.
     * @throws UnsupportedOperationException if the underlying data collection is immutable
     */
    fun add(`object`: T) {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                mOriginalValues!!.add(`object`)
            } else {
                mObjects!!.add(`object`)
            }
            mObjectsFromResources = false
        }
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    /**
     * Adds the specified Collection at the end of the array.
     *
     * @param collection The Collection to add at the end of the array.
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     * is not supported by this list
     * @throws ClassCastException if the class of an element of the specified
     * collection prevents it from being added to this list
     * @throws NullPointerException if the specified collection contains one
     * or more null elements and this list does not permit null
     * elements, or if the specified collection is null
     * @throws IllegalArgumentException if some property of an element of the
     * specified collection prevents it from being added to this list
     */
    fun addAll(collection: Collection<T>) {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                mOriginalValues!!.addAll(collection)
            } else {
                mObjects!!.addAll(collection)
            }
            mObjectsFromResources = false
        }
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    /**
     * Adds the specified items at the end of the array.
     *
     * @param items The items to add at the end of the array.
     * @throws UnsupportedOperationException if the underlying data collection is immutable
     */
    fun addAll(vararg items: T) {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                Collections.addAll<T>(mOriginalValues!!, *items)
            } else {
                Collections.addAll<T>(mObjects!!, *items)
            }
            mObjectsFromResources = false
        }
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    /**
     * Inserts the specified object at the specified index in the array.
     *
     * @param object The object to insert into the array.
     * @param index The index at which the object must be inserted.
     * @throws UnsupportedOperationException if the underlying data collection is immutable
     */
    fun insert(`object`: T, index: Int) {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                mOriginalValues!!.add(index, `object`)
            } else {
                mObjects!!.add(index, `object`)
            }
            mObjectsFromResources = false
        }
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    /**
     * Removes the specified object from the array.
     *
     * @param object The object to remove.
     * @throws UnsupportedOperationException if the underlying data collection is immutable
     */
    fun remove(`object`: T) {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                mOriginalValues!!.remove(`object`)
            } else {
                mObjects!!.remove(`object`)
            }
            mObjectsFromResources = false
        }
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    /**
     * Remove all elements from the list.
     *
     * @throws UnsupportedOperationException if the underlying data collection is immutable
     */
    fun clear() {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                mOriginalValues!!.clear()
            } else {
                mObjects!!.clear()
            }
            mObjectsFromResources = false
        }
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained
     * in this adapter.
     */
    fun sort(comparator: Comparator<in T>) {
        synchronized(mLock) {
            if (mOriginalValues != null) {
                Collections.sort(mOriginalValues!!, comparator)
            } else {
                Collections.sort(mObjects!!, comparator)
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        mNotifyOnChange = true
    }

    /**
     * Control whether methods that change the list ([.add], [.addAll],
     * [.addAll], [.insert], [.remove], [.clear],
     * [.sort]) automatically call [.notifyDataSetChanged].  If set to
     * false, caller must manually call notifyDataSetChanged() to have the changes
     * reflected in the attached view.
     *
     * The default is true, and calling notifyDataSetChanged()
     * resets the flag to true.
     *
     * @param notifyOnChange if true, modifications to the list will
     * automatically call [                       ][.notifyDataSetChanged]
     */
    fun setNotifyOnChange(notifyOnChange: Boolean) {
        mNotifyOnChange = notifyOnChange
    }

    override fun getCount(): Int {
        return mObjects!!.size
    }

    override fun getItem(position: Int): T? {
        return mObjects?.get(position)
    }

    /**
     * Returns the position of the specified item in the array.
     *
     * @param item The item to retrieve the position of.
     *
     * @return The position of the specified item.
     */
    fun getPosition(item: T): Int {
        return mObjects!!.indexOf(item)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    
    override fun getView(position: Int, convertView: View?,
                         parent: ViewGroup): View {
        return createViewFromResource(mInflater, position, convertView, parent, mResource)
    }

    
    private fun createViewFromResource(inflater: LayoutInflater, position: Int,
                                       convertView: View?, parent: ViewGroup, resource: Int): View {
        val view: View = convertView ?: inflater.inflate(resource, parent, false)
        val text: TextView?

        try {
            if (mFieldId == 0) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = view as TextView
            } else {
                //  Otherwise, find the TextView field within the layout
                text = view.findViewById(mFieldId)

                if (text == null) {
                    throw RuntimeException("Failed to find view with ID "
                            + context.resources.getResourceName(mFieldId)
                            + " in item layout")
                }
            }
        } catch (e: ClassCastException) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView")
            throw IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e)
        }

        val item = getItem(position)
        if (item is CharSequence) {
            text.text = item
        } else {
            text.text = item.toString()
        }

        return view
    }

    /**
     *
     * Sets the layout resource to create the drop down views.
     *
     * @param resource the layout resource defining the drop down views
     * @see .getDropDownView
     */
    fun setDropDownViewResource(@LayoutRes resource: Int) {
        this.mDropDownResource = resource
    }

    override fun getDropDownView(position: Int, convertView: View?,
                                 parent: ViewGroup): View {
        val inflater = if (mDropDownInflater == null) mInflater else mDropDownInflater
        return createViewFromResource(inflater!!, position, convertView, parent, mDropDownResource)
    }

    
    override fun getFilter(): Filter {
        if (mFilter == null) {
            mFilter = ArrayFilter()
        }
        return mFilter!!
    }

    /**
     * {@inheritDoc}
     *
     * @return values from the string array used by [.createFromResource],
     * or `null` if object was created otherwsie or if contents were dynamically changed after
     * creation.
     */
    override fun getAutofillOptions(): Array<CharSequence>? {
        // First check if app developer explicitly set them.
        val explicitOptions = super.getAutofillOptions()
        if (explicitOptions != null) {
            return explicitOptions
        }

        // Otherwise, only return options that came from static resources.
        if (!mObjectsFromResources || mObjects == null || mObjects!!.isEmpty()) {
            return null
        }
        return mObjects?.map { it.toString() }?.toTypedArray()
    }

    /**
     *
     * An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.
     */
    private inner class ArrayFilter : Filter() {
        override fun performFiltering(prefix: CharSequence?): FilterResults {
            val results = FilterResults()

            if (mOriginalValues == null) {
                synchronized(mLock) {
                    mOriginalValues = ArrayList(mObjects!!)
                }
            }

            if (prefix == null || prefix.isEmpty()) {
                val list: ArrayList<T>
                synchronized(mLock) {
                    list = ArrayList(mOriginalValues!!)
                }
                results.values = list
                results.count = list.size
            } else {
                val prefixString = prefix.toString().getNormalizedString().toLowerCase()

                val values: ArrayList<T>
                synchronized(mLock) {
                    values = ArrayList(mOriginalValues!!)
                }

                val count = values.size
                val newValues = ArrayList<T>()

                for (i in 0 until count) {
                    val value = values[i]
                    val valueText = value.toString().getNormalizedString().toLowerCase()

                    // First match against the whole, non-splitted value
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(value)
                    } else {
                        val words = valueText.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        for (word in words) {
                            if (word.startsWith(prefixString)) {
                                newValues.add(value)
                                break
                            }
                        }
                    }
                }
                if (newValues.size == 0 && emptyItem != null) {
                    newValues.add(emptyItem!!)
                }
                results.values = newValues
                results.count = newValues.size
            }

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            this@ArrayAdapterNormalized.constraint = constraint
            mObjects = results.values as MutableList<T>
            if (results.count > 0) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }
}