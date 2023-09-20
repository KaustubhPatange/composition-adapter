# composition-adapter

A simplified way of writing RecyclerView Adapter through Kotlin DSL promoting composition over
inheritance. It internally creates a [Paging3 Adapter](#q-why-paging3-adapter).

## Features

- Create adapters/viewHolder using Kotlin DSL.
- [Lifecycle aware](#q-lifecycle-aware-viewholder) View Holders.
- Built-in support for View binding.
- Loading state adapters (show loaders during first load or during load more).
- Less boilerplate to write.

## Quick Setup

![Maven Central](https://img.shields.io/maven-central/v/io.github.kaustubhpatange/compose-adapter)

- Add dependency or directly use the `compose-adapter` module in your project.

```groovy
implementation("io.github.kaustubhpatange:compose-adapter:$LATEST")
```

- Define adapter and ViewHolder(s) for your RecyclerView.

```kotlin
val adapter = composeAdapter {
    addHolder(
        DataClass::class,
        DataClassViewBinding::inflate
    ) {
        onBind = { item, _ ->
            with(this.binding) {
              this.textView.text = ...
              // ^? "this" is your ViewBinding context.
            }
        }
    }
}

recyclerView.adapter = adapter
```

- A [sample](sample/) app is available which implements,
  - **Paging Example**: A compelete network first paging example.
  - **Nested RecyclerView Example**: An example which implements nested `RecyclerView` using composition adapter.

## Apps using `composition-adapter`

|                                                         |                                                                                                                                                                                                                                                                                                                                   |
| ------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| <img width="300" src="art/pratilipi-comics-demo.gif" /> | [Pratilipi Comics](https://play.google.com/store/apps/details?id=com.pratilipi.comics) is the most popular comic reading platform in India with over 10+ million downloads. <br><br> The home page is a paging list using `composition-adapter` having over 20+ `ViewHolder`s. <br><br> _Gif is running in 8fps with 1.5x speed._ |

## FAQs

### Q. Why Paging3 adapter?

`PagingDataAdapter` is used to support closed range data as well as open ended (paginated data).
With it's async diffutil strategy through `AsyncPagingDataDiffer`, updates are computed on a
background thread which provides performance improvement over synchronous updates. This is typically
helpful for a large dataset.

You don't need `RemoteMediator` for synchronizing local DB and network updates instead you can use
plain simple `PagingSource` with your custom logic to deal with this use case or a better way would
be to rely on HTTP cache-control headers which should be automatically respected by any HTTP client.
This way you don't need to overcomplicate things by using `RemoteMediator`.

As it is generally nothing but a `RecyclerView.Adapter`, we can also use a non-paging (static) data.

### Q. Lifecycle aware ViewHolder

`ViewHolder`s in compose adapter are lifecycle aware which means you can observe `LiveData` / `Flow`
or launch a coroutine safely within `onBind`, `onCreate`. You will get appropriate state changes
like `ON_CREATE`, `ON_RESUME`, `ON_STOP` based on when `RecyclerView` tries to recycle views.

Use `lifecycleCoroutineScope` to launch coroutines. Since `onBind` is called multiple times, it is best to collect `Flow` or `LiveData` during `onAttach`.

# License

- [The MIT License](LICENSE)

```
Copyright (c) 2022 Kaustubh Patange

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
