;; (put-clojure-indent 'forall 1)

(ns cygnus-www.core
  (:require-macros [cygnus-www.macros :refer [defui forall defpage]])
  (:require [cygnus-www.code :as code]
            [cygnus-www.state :as state]
            [reagent.core :as reagent :refer [atom]]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]))

(defn scroll-to-top []
  (letfn [(timefn []
            (when (not= 0 (.-scrollY js/window))
              (.scrollBy js/window 0 -80)
              (js/setTimeout timefn 16)))]
    (timefn)))

(defui code-box [strs & [default]] {:tab (or default 0)}
  [:div.code-box
   [:div.tab-bar
    (forall [[i {name :name}] (map-indexed vector strs)] ^{:key [i name]}
      [:span.tab
       {:on-click #(swap! self assoc :tab i)
        :class (when (= (:tab @self) i) "active")}
       name])]
   [:div.code
    (let [{body :body} (get strs (:tab @self))]
      [:pre [:code.rust body]])]])

(defpage index {}
  [:div
   [:header
    [:section#top
     [:h1 [:a {:href "/"} "Cygnus"]]
     [:nav
      [:a {:href "/tutorial"} "Tutorial"]
      [:a {:href "https://docs.adamniederer.com"} "Docs"]
      [:a {:href "https://github.com/AdamNiederer/faster"} "Code"]]]
    [:section#banner
     [:h1 "Light speed code"]
     [:p "Speed up your data processing tasks without sacrificing readability"]
     [:span.release "Release 0.5.0"]
     [:section#banner-features
      [:div [:div.icon.lg] [:h2 "Efficient"]
       [:span "Cygnus is a " [:em "zero-overhead"] " library, and uses every trick in the book to save cycles."]]
      [:div [:div.icon.lg] [:h2 "Portable"]
       [:span "Cygnus compiles and runs on " [:em "any architecture"] ", letting you write once and run anywhere."]]
      [:div [:div.icon.lg] [:h2 "Usable"]
       [:span "Waste no developer time. Cygnus handles the caveats of " [:em "vector algorithms"] " for you."]]]]]
   [:main
    [:section#tutorial
     [:div.tutorial-part
      [:div.tutorial-text
       [:h1 "Boldly go"]
       [:p "Cygnus makes prototyping SIMD code easy. Map reduce, and collect using Cygnus' vector-aware methods to write code which produces the correct result on any platform."]]
      [code-box code/blast-off]]
     [:div.tutorial-part
      [:div.tutorial-text
       [:h1 "Waste not"]
       [:p "Cygnus compiles down to exactly what you would write by hand. Let LLVM handle the optimization so you can focus on writing good code."]]
      [code-box code/simple-disassembly]]
     [:div.tutorial-part
      [:div.tutorial-text
       [:h1 "Want not"]
       [:p "Cygnus implements everything from simple arithmetic to the "
        [:a {:href "https://arxiv.org/abs/1611.07612"} "Muła-Kurz-Lemire Vector Popcnt"]
        ". If your architecture doesn't support an operation, a fast scalar alternative will be used."]]
      [code-box code/hamming-distance]]]
    [:section#appeal
     [:h1 "Learn more in the guide"]
     [:a.button.em {:href "/tutorial"} "Guide"]
     [:a.button {:href "https://docs.adamniederer.com"} "API Reference"]]]
   [:footer "Copyright 2017-2018 Adam Niederer"]])

(defui tutorial-intro [] {}
  [:div#intro.tutorial
   [:h1 "What is SIMD?"]
   [:p "SIMD a method of computation which processes multiple pieces of simple data at once. Nearly every modern processor has some kind of SIMD extension, such as SSE and AVX on x86, NEON and ASIMD on ARM, MSA on MIPS, and Extension V on RISC-V. SIMD code can often process data much more quickly than regular code, but it can be difficult to write."]
   [:p "At the heart of SIMD is the idea of vectors. " [:em "Vectors are small contiguous collections of primitive types."] " For example, two adjacent u64s form a 128-bit vector. All SIMD architectures have large registers which can hold vectors. AVX uses 256-bit vector registers, while NEON uses 128-bit registers."]
   [:p "SIMD architectures define instructions which can perform a single operation on every element of a vector. For example, the AVX instruction " [:code "vpaddb dest, src1, src2"] " adds each element in src1 to the element at the same position in src2, and stores it at that position in dest. The power of such an instruction should be apparent: With a 256-bit vector of bytes, we can perform 32 additions in a single cycle!"]
   [:h2 "Why isn't everybody using SIMD?"]
   [:p "While SIMD can deliver very impressive performance results, there are many reasons to avoid writing SIMD code. Here are a few:"]
   [:ul
    [:li "It isn't portable, even within the same architecture"]
    [:li "It involves more edge cases, which generate more bugs"]
    [:li "It can't be used for every type of problem"]
    [:li "It's difficult to use for protyping"]
    [:li "It's difficult to read and maintain"]
    [:li "The compiler can sometimes do it for you"]
    [:li "It often needs an equivalent scalar algorithm accompanying it"]]
   [:p "Cygnus aims to address many of these issues, and make writing SIMD code worthwhile in more scenarios. Below is an example comparing Cygnus to equivalent SSE code, as well as a scalar equivalent for reference. Don't worry about the code yet, but notice the awkardness of the SIMD code compared to the scalar code. Without looking at the assert, would you be able to tell what the explicit SIMD code is doing at a glance?"]
   [code-box code/blast-off 1]
   [:h2 "When should I consider using SIMD?"]
   [:p "Despite SIMD's limitations, there are many conditions under which using SIMD is a good idea. Here are a few:"]
   [:ul
    [:li "Your code needs to be as fast as possible"]
    [:li "Multithreading is too slow, or you've already multithreaded your code"]
    [:li "Offloading to a GPU has too much overhead, or there is no GPU available"]
    [:li "You are operating on many primitive types at once"]
    [:li "Your algorithm is convergent, and avoids branching"]]
   [:h2 "How can Cygnus help me use SIMD?"]
   [:p "Cygnus alleviates many of the issues with writing explicit SIMD. With Cygnus, the list of reasons to not use SIMD shrinks considerably:"]
   [:ul
    [:li.strike "It isn't portable, even within the same architecture"]
    [:li.strike "It involves more edge cases, and more bugs"]
    [:li "It can't be used for every type of problem"]
    [:li.strike "It's difficult to use for protyping"]
    [:li.strike "It's difficult to read and maintain"]
    [:li "The compiler can sometimes do it for you"]
    [:li.strike "It often needs an equivalent scalar algorithm accompanying it"]]
   [:p "Cygnus makes your SIMD code portable. Cygnus contains SIMD implementations of many common vector operations for many architectures, and has a scalar backup if your target is incapable of performing the operation. Often, these scalar backups are able to make use of the superscalar architecture of modern CPUs, and are faster than a naiive solution. Write it once, run it everywhere."]
   [:p "Improving ease of writing, reading, and maintaining SIMD code is Cygnus' core mission. Cygnus presents a memory-safe API which handles the many edge cases found in SIMD code, as well as an unsafe API for experts looking to squeeze every bit of performance out of their code while benefitting from Cygnus' breezy syntax and portability."]
   [:p "Cygnus' scalar backups and highly efficient partial-aware iterator system eliminate the need for an accompanying scalar algorithm with your code. Write the vector algorithm, and all of your data can use it."]])

(defn zor [x alt]
  (if (= x 0) alt x))

(defui tutorial-vectors [] {:ctor "splat" :n 0 :m 0 :k 0 :vec-len 4}
  [:div#vectors.tutorial
   [:h1 "Vectors"]
   [:p "Vectors can be thought of as short, contiguous arrays of a primitive type. Cygnus provides vectors named " [:code "u8s, i8s, u16s, i16s, u32s, i32s, f32s, u64s, i64s"] ", and " [:code "f64s"] ". Each vector contains some number of its respective primitive type. The number of elements in each vector is determined by your target platform."]
   [:h2 "Constructors"]
   [:p "There are a few different ways to create a vector. " [:em "To ensure portability across platforms, constructing a vector with " [:code "new"] " isn't recommended."] " Using " [:code "u8s"] " as an example, here are the vector constructors:"]
   [:ul
    [:li [:code "u8s::new(a, b, ...)"] [:div "Creates a vector where the first element is initialized to a, the second to b, etc. The arity of this function changes depending on the compilation target!"]]
    [:li [:code "u8s::splat(n)"] [:div "Creates a vector with all elements initialized to n"]]
    [:li [:code "u8s::halves(n, m)"] [:div "Creates a vector with its first half initialized to n, and its second half to m"]]
    [:li [:code "u8s::interleave(n, m)"] [:div "Creates a vector with its even elements initialized to n, and its odd elements to m"]]
    [:li [:code "u8s::partition(n, m, k)"]
     [:div "Creates a vector with the first k elements initialized to n, and the rest to m"]]]
   [:p "These constructors work for every vector type. " [:em "Because " [:code "splat"] " is so commonly used, it is also aliased to the name of the vector. "] [:code "u8s(0)"] " does the same thing as " [:code "u8s::splat(0)"] ". Below is an interactive demo showcasing each vector constructor."]
   [:div.flex.figure.w-left.h-center
    [:code "u8s::"]
    [:span [:select {:on-change #(swap! self assoc :ctor (-> % .-target .-value))}
            [:option {:value "splat" :selected true} "splat"]
            [:option {:value "halves"} "halves"]
            [:option {:value "interleave"} "interleave"]
            [:option {:value "partition"} "partition"]]]
    [:code "("]
    [:span
     [:input.single {:placeholder "n"
                     :on-change #(swap! self assoc :n (int (-> % .-target .-value)))}]]
    (when (not= (:ctor @self) "splat")
      [:span [:code ", "]
       [:input.single {:placeholder "m"
                       :on-change #(swap! self assoc :m (int (-> % .-target .-value)))}]])
    (when (= (:ctor @self) "partition")
      [:span [:code ", "]
       [:input.single {:placeholder "k"
                       :on-change #(swap! self assoc :k (-> % .-target .-value))}]])
    [:code ")"]
    [:input.flex-right {:placeholder "Vector Length"
                        :on-change #(swap! self assoc :vec-len (-> % .-target .-value))}]]
   (let [[ctor len n m k] (map #(% @self) [:ctor :vec-len :n :m :k])
         half-len (int (/ len 2))
         k (max 0 k)]
     [:div.collection.figure
      (case ctor
        "splat" (for [i (range len)] [:div.collection-element n])
        "halves" (for [i (concat (repeat half-len n)
                                 (repeat half-len m))]
                   [:div.collection-element i])
        "interleave" (for [i (interleave (repeat half-len n)
                                         (repeat half-len m))]
                       [:div.collection-element i])

        "partition" (for [i (concat (repeat (min len (int k)) n)
                                    (repeat (max 0 (- len k)) m))]
                      [:div.collection-element i]))])
   [:h2 "Vector Operations"]
   [:p "Most vector operations operate on all elements of the vector, possibly with elements of another vector at the same index. The examples below should make this more intuitively clear:"]
   [:div.flex.figure
    [:div.collection (for [i (range 4)] [:div.collection-element i])
     [:code " + "]
     (for [i (range 4)] [:div.collection-element i])
     [:code " => "]
     (for [i (range 4)] [:div.collection-element (* 2 i)])]]
   [:div.flex.figure
    [:div.collection (for [i [1 2 3 4]] [:div.collection-element i])
     [:code ".max("]
     (for [i [4 3 2 1]] [:div.collection-element i])
     [:code ") => "]
     (for [i [4 3 3 4]] [:div.collection-element i])]]
   [:div.flex.figure
    [:div.collection (for [i [1 2 3 4]] [:div.collection-element i])
     [:code ".sqrt() => "]
     (for [i [1 1.4 1.7 2]] [:div.collection-element i])]]
   [:p "There are some operations which will modify the structure of a vector, or return a scalar value based on the contents of the vector. "
    "Check the API reference for each intrinsic to get more information about it."]
   [:div.flex.figure
    [:div.collection (for [i (range 4)] [:div.collection-element "3"])
     [:code ".count_ones() => 8"]]]
   [:div.flex.figure
    [:div.collection (for [i (range 4)] [:div.collection-element "3"])
     [:code ".sum() => 12"]]]
   [:div.flex.figure
    [:div.collection (for [i [1 2 3 4]] [:div.collection-element i])
     [:code ".flip() => "]
     (for [i [2 1 4 3]] [:div.collection-element i])]]
   [:p "On a machine which supports SIMD, most vector operations will compile down to one or two instructions. These instructions usually take only a few cycles to execute - some architectures can execute many simple SIMD instructions per cycle."]])

(defui tutorial-iterators [] {:vec-len 4 :coll-len 11 :default ""}
  [:div#iterators.tutorial
   [:h1 "Simple Iterators"]
   [:p "Cygnus' iterator system aims to be as similar to Rust's as possible. To create a SIMD iterator, simply call " [:code "into_simd_iter"] ", " [:code "simd_iter"] ", or " [:code "simd_iter_mut"] " on a collection, depending on your mutability and ownership preferences. Like Rust's " [:code "into_iter"] ", " [:em [:code "into_simd_iter"] " allocates a vector on the heap"] "."]
   [:h2 "Using Simple Iterators"]
   [:p "It's worth noting that like Rust's iterators, " [:em "Cygnus' iterators are lazy"] ". No work will be done until a function \"consumes\" the iterator. The simplest function which consumes iterators is " [:code "scalar_collect"] ". It, like Rust's " [:code "collect"] ", consumes all of the values of your iterator and stores them in a vector. If you'd like to avoid an allocation or aren't using the standard library, " [:code "scalar_fill"] " does the same thing to a slice."]
   [:p "We can use this to write a very fast copy function."]
   [code-box code/memcpy]
   [:p "You may have noticed that " [:code "simd_iter"] " and friends take an argument. This is a vector of " [:em "default values"] ", which fill the unused slots of a partially-filled vector. This is necessary to make your code as clean and as fast as possible ; we'll cover why in the next heading."]
   [:h2 "Vector Patterns"]
   [:p "Many SIMD architectures can't easily load and store collections which don't evenly fit into SIMD vectors. For example, most versions of x86 have issues with collections whose lengths aren't multiples of 4. Often, this issue is worked around by providing a vector algorithm to process the majority of the data, and a scalar algorithm to process the remainder which won't fit into a vector. That's ugly and slow, though. Cygnus uses a special load/store pattern which lets you avoid providing a scalar algorithm and taking the associated performance hit."]
   [:p "All vectors except the last one are loaded as one would expect. However, " [:em "the last vector's contents are shifted all the way to the right, and contain a user-provided default value in the unfilled slots"] ". This is to ensure we can load all of the remaining data into the last vector in a single instruction on all platforms."]
   [:p "Here's an interactive example showing how Cygnus will split a large, uneven collection into smaller vectors. Pay special attention to how the last (rightmost) vector behaves."]
   [:div.flex.figure
    [:input {:placeholder "Vector Size"
             :on-change #(swap! self assoc :vec-len (int (-> % .-target .-value)))}]
    [:input {:placeholder "Collection Size"
             :on-change #(swap! self assoc :coll-len (int (-> % .-target .-value)))}]
    [:input {:placeholder "Default Value"
             :on-change #(swap! self assoc :default (-> % .-target .-value))}]]
   (let [coll-len (zor (:coll-len @self) 11)
          vec-len (zor (:vec-len @self) 4)
          values (take coll-len (repeatedly #(rand-int 30)))]
     [:div.figure
      [:div.collection.figure
       (for [value values]
         [:div.collection-element value])]
      [:div.collection
       (for [chunk (partition vec-len vec-len nil values)]
         [:div.collection.figure
          (when (> vec-len (count chunk))
            (for [i (range (- vec-len (count chunk)))]
              [:div.collection-element (:default @self)]))
          (for [value chunk]
            [:div.collection-element value])])]])
   [:p "Thankfully, using Cygnus' simple iterator system is much easier than understanding it - Cygnus handles all of the loading and storing for you, so you can usually pretend you're using a normal iterator. Cygnus knows not to store the elements of the vector which were filled with the default, but they do come into play when doing reductive or swizzling operations. We'll go in-depth on that later in this tutorial. Users who wish to avoid this system may also be interested in the Manual Iterators section."]])

(defui tutorial-map [] {}
  [:div
   [:h1 "Mapping"]
   [:p [:em "Mapping lets us apply a vector operation to every element in a collection."] " To perform a mapping operation, simply create an iterator and call " [:code "simd_map"] " with a closure, just like Rust's " [:code "map"] " function. Your closure will be passed vectors containing the elements of the underlying collection. The exact order and position of elements in the vectors is described in the iterators tutorial."]
   [:p "Also like Rust's " [:code "map"] ", " [:code "simd_map"] " returns a lazy iterator which applies the closure to a vector of elements, and yields the vector. You can chain " [:code "simd_map"] " with any other SIMD-aware function, like " [:code "scalar_collect"] "."]
   [:p "To ensure compatibility with other SIMD-aware functions, " [:em "the closure passed to " [:code "simd_map"] " must return a single vector"] ". Closures which return something else can be passed to Rust's standard " [:code "map"] " function to achieve similar results. See the section on Manual Iterators for more information."]
   [code-box code/tcp-checksum-map]
   [:h2 "Invariants"]
   [:p "Mapping operations usually preserve the order and size of the collection, but " [:code "simd_map"] [:em " lets you reorder and resize the collection"] " in certain ways. For example, calling " [:code "flip"] " in your closure will swap every even element with the corresponding odd element."]
   [:div.flex.figure.w-center
    [:div.collection (for [i [1 2 3 4]] [:div.collection-element i])
     [:code ".flip() => "]
     (for [i [2 1 4 3]] [:div.collection-element i])]]
   [:p "If this happens, your code will not preserve the order of the collection,  " [:em " and might shuffle a default value into your result"] ". That's not necessarily a bad thing, but you should be aware of your code's specific invariants when using mapping operations, as many invariants of the classical " [:code "map"] " operation are not present in " [:code "simd_map"] "."]
   [:p "Cygnus also lets you modify the number of elements in your collection via " [:code "simd_map"] " with casting operations. Functions like " [:code "be_u8s"] " or " [:code "be_i32s"] " are simple bitcasts which reinterpret the type of element within the vector. Upcasting and downcasting can also complicate things. In the above code sample, the input has four times as many elements as the output."]])

(defui tutorial-reduce [] {}
  [:div
   [:h1 "Reductive Operations"]
   [:p "Not every SIMD computation is a mapping operation. Sometimes, we can exploit SIMD in computations which take a collection of elements, and return a single element."]
   [:h2 "Vector Reduction"]
   [:p "Like Rust's " [:code "fold"] ", " [:code "simd_reduce"] " accepts an initial value of the accumulator a closure which takes the accumulator and an element of your iterator and returns the new accumulator." [:em " Your iterator's default value will usually be used"] " if you're not overwriting it in a " [:code "simd_map"] ", so make sure it doesn't unexpectedly alter the value of the accumulator. Usually, 0 or 1 is a good choice for a default value, depending on what you're doing."]
   [:h2 "Scalar Reduction"]
   [:p "Vector reduction results in a vector, but the returned vector rarely has much meaning. You'll need to reduce the vector oncemore into a scalar. Cygnus provides specialized functions which can reduce a vector into a scalar using SIMD intrinsics like " [:code "sum"] " or " [:code "product"] ", but more complex operations can be performed with the versatile " [:code "scalar_reduce"] "."]])

(defui tutorial-collect [] {}
  [:div "Coming soon!"])
(defui tutorial-stride [] {}
  [:div "Coming soon!"])
(defui tutorial-swizzle [] {}
  [:div "Coming soon!"])
(defui tutorial-advanced-iterators [] {}
  [:div "Coming soon!"])
(defui tutorial-benchmark [] {}
  [:div "Coming soon!"])
(defui tutorial-cookbook [] {}
  [:div "Coming soon!"])

(def tutorial-pages
  [{:name "Introduction" :page tutorial-intro}
   {:name "Vectors" :page tutorial-vectors}
   {:name "Iterators" :page tutorial-iterators}
   {:name "Mapping" :page tutorial-map}
   {:name "Reduction" :page tutorial-reduce}
   {:name "Collection" :page tutorial-collect}
   {:name "Striding" :page tutorial-stride}
   {:name "Swizzling" :page tutorial-swizzle}
   {:name "Manual Iterators" :page tutorial-advanced-iterators}
   {:name "Benchmarking" :page tutorial-benchmark}
   {:name "Cookbook" :page tutorial-cookbook}])

(defpage tutorial {:page tutorial-reduce}
  [:div
   [:header
    [:section#top
     [:h1 [:a {:href "/"} "Cygnus"]]
     [:nav
      [:a {:href "/tutorial"} "Tutorial"]
      [:a {:href "https://docs.adamniederer.com"} "Docs"]
      [:a {:href "https://github.com/AdamNiederer/faster"} "Code"]]]
    [:section#banner
     [:h1 "Guide"]
     [:p "Find you way around Cygnus without a star chart"]
     [:span.release "Updated for release 0.5.0"]]]
   [:main
    [:aside
     [:h3 "The Cygnus Guide"]
     [:ol
      (forall [{name :name page :page} tutorial-pages]
        [:li.click {:on-click #(swap! self assoc :page page)
                    :class (when (= (:page @self) page) "active")}
         name])]]
    [:section.tutorial
     [((:page @self))]]]
   [:footer "Copyright 2017-2018 Adam Niederer"]])

(secretary/defroute "/" []
  (reset! state/page index))

(defn mount-root []
  (accountant/dispatch-current! true) ; For figwheel
  (reagent/unmount-component-at-node (.getElementById js/document "app"))
  (reagent/render [state/current-page] (.getElementById js/document "app")))

(defn init! []
  (enable-console-print!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path noscroll?]
      (when (not noscroll?) (scroll-to-top))
      (secretary/dispatch! path))
    :path-exists?
    (fn [path]
      (secretary/locate-route path))
    :reload-same-path? true})
  (accountant/dispatch-current! true)
  (mount-root))
