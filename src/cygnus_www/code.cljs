(ns cygnus-www.code)

(def blast-off
  [{:name "Faster"
    :body "let inc = [1, 2, 3, 4, 5]
    .simd_iter(u8s(0))
    .simd_map(|v| v + 1)
    .scalar_collect();

assert_eq!(inc, vec![2, 3, 4, 5, 6]);"}
   {:name "Explicit SIMD"
    :body "let simdvec: __m128 = unsafe { mem::uninitialized() };
let add = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1];
let arr = [1, 2, 3, 4, 5];
let inc = Vec::new();

for i, el in arr.iter().enumerate() {
    _mm_insert_epi8(simdvec, el, i);
}

_mm_load_epi8(add);
_mm_add_epi8(simdvec, add);

for i in 0..arr.len() {
    inc.push(_mm_extract_epi8(simdvec, i));
}

assert_eq!(inc, vec![2, 3, 4, 5, 6]);"}
   {:name "Scalar"
    :body "let inc = [1, 2, 3, 4, 5]
    .iter()
    .map(|v| v + 1)
    .collect::<Vec<u8>>();

assert_eq!(inc, vec![2, 3, 4, 5, 6]);"}])

(def simple-disassembly
  [{:name "Faster"
    :body "pub fn cksum() -> u32 {
    [1u8; 900].simd_iter(u8s(0))
        .simd_map(|s| {
            let (a, b) = s.be_u16s().from_be().upcast();
            a + b
        })
        .simd_reduce(u32s(0), |acc, v| acc + v)
        .sum() as u32
}"}
   {:name "Disassembly"
    :body "faster_test::cksum
        push	rbp
        mov	rbp, rsp
        vpxor	xmm1, xmm1, xmm1
        mov	eax, 16
        lea	rcx, [rip + .Lref.9]
        vmovdqa	xmm0, xmmword ptr [rip + .LCPI0_0]
.LBB0_1:
        vmovdqu	xmm2, xmmword ptr [rax + rcx - 16]
        vmovdqu	xmm3, xmmword ptr [rax + rcx]
        vpshufb	xmm2, xmm2, xmm0
        vpmovzxwd xmm4, xmm2
        vpshufd	xmm2, xmm2, 78
        vpmovzxwd xmm2, xmm2
        vpaddd	xmm1, xmm4, xmm1
        vpaddd	xmm1, xmm1, xmm2
        vpshufb	xmm2, xmm3, xmm0
        vpmovzxwd xmm3, xmm2
        vpshufd	xmm2, xmm2, 78
        vpmovzxwd xmm2, xmm2
        vpaddd	xmm2, xmm3, xmm2
        vpaddd	xmm1, xmm1, xmm2
        add	rax, 32
        cmp	rax, 901
        jb	.LBB0_1
        vpaddd	xmm0, xmm1, xmmword ptr [rip + .LCPI0_1]
        vmovd	eax, xmm0
        vpextrd	ecx, xmm0, 1
        add	ecx, eax
        vpextrd	edx, xmm0, 2
        add	edx, ecx
        vpextrd	eax, xmm0, 3
        add	eax, edx
        pop	rbp
        ret"}])

(def hamming-distance
  [{:name "Hamming Distance"
    :body "pub fn hamming_distance(x: &[u8], y: &[u8]) -> usize {
    assert_eq!(x.len(), y.len());

    (x.simd_iter(u8s(0)), y.simd_iter(u8s(0))).zip()
        .simd_reduce(0, |ac, (a, b)| ac + (a ^ b).count_ones())
}"}
   {:name "Matrix Determinant"
    :body "pub fn determinant3(matrices: &[f32]) -> Vec<f32> {
    assert!(matrices.len() % 9 == 0);

    matrices.stride_nine(tuplify!(9, f32s(0.0))).zip()
        .simd_map(|(a, b, c, d, e, f, g, h, i)| {
            (a * e * i) + (b * f * g) + (c * d * h)
                - (c * e * g) - (b * d * i) - (a * f * h)
        }).scalar_collect())
}"}
   {:name "Character Count"
    :body "pub fn num_chars(haystack: &[u8]) -> usize {
    let chunk = (u8s::WIDTH * 255);
    let all = haystack.len();
    let count = |hs| {
        hs.simd_iter().simd_reduce(u8s(0), u8s(0), |acc, v| {
            acc + (v & u8s(0xC0)).eq(u8s(0x80)) & u8s(0x01))
        }).scalar_reduce(0, |acc, s| acc + (s as usize))
    }

    let mut ret = 0;
    for i in 0..haystack.len() / chunk {
        ret += count(haystack[i * chunk..(i + 1) * chunk]))
    }
    all - ret - count((&haystack[all - all % chunk..]))
}"}
   {:name "TCP Checksum"
    :body "pub fn checksum(data: &[u8], final_byte: u16) -> u16 {
    final_byte + data.simd_iter(u8s(0))
        .simd_map(|v| {
             let (a, b) = v.be_u16s().from_be().upcast();
             a + b
        })
        .simd_reduce(u32s(0), |acc, v| acc + v)
        .scalar_reduce(0u32, |acc, s| acc.overflowing_add(s).0)
}"}])

(def memcpy
  [{:name "Vector Copy"
    :body "fn copy_vector(vec: Vec<u8>) -> Vec<u8> {
    vec.simd_iter(u8s(0)).scalar_collect()
}"
    }
{:name "Slice Copy"
    :body "fn copy_slice(out: &'a mut [u8], vec: &'b [u8]) -> &'a mut [u8] {
    vec.simd_iter(u8s(0)).scalar_fill(out)
}"}])

(def tcp-checksum-map
  [{:name "Partial TCP Checksum"
     :body "pub fn checksum(data: &[u8]) {
    data.simd_iter(u8s(0))
        .simd_map(|v| {
             let (a, b) = v.be_u16s().from_be().upcast();
             a + b
        }).scalar_collect();
}"}])
