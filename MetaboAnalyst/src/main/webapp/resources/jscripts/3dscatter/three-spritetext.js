// Version 1.0.6 three-spritetext - https://github.com/vasturiano/three-spritetext
bgColor = "#222222"
bgColor2 = "#ffffff"
! function(t, e) {
    "object" == typeof exports && "undefined" != typeof module ? module.exports = e(require("three")) : "function" == typeof define && define.amd ? define(["three"], e) : t.SpriteText = e(t.THREE)
}(this, function(t) {
    "use strict";
    var e = function() {
            function i(t, e) {
                for (var n = 0; n < e.length; n++) {
                    var i = e[n];
                    i.enumerable = i.enumerable || !1, i.configurable = !0, "value" in i && (i.writable = !0), Object.defineProperty(t, i.key, i)
                }
            }
            return function(t, e, n) {
                return e && i(t.prototype, e), n && i(t, n), t
            }
        }(),
        r = window.THREE ? window.THREE : {
            LinearFilter: t.LinearFilter,
            Sprite: t.Sprite,
            SpriteMaterial: t.SpriteMaterial,
            Texture: t.Texture
        };
    return function(t) {
        function o() {
            var t = 0 < arguments.length && void 0 !== arguments[0] ? arguments[0] : "",
                e = 1 < arguments.length && void 0 !== arguments[1] ? arguments[1] : 10,
                n = 2 < arguments.length && void 0 !== arguments[2] ? arguments[2] : "#222222";

            ! function(t, e) {
                if (!(t instanceof e)) throw new TypeError("Cannot call a class as a function")
            }(this, o);
            var i = function(t, e) {
                if (!t) throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
                return !e || "object" != typeof e && "function" != typeof e ? t : e
            }(this, (o.__proto__ || Object.getPrototypeOf(o)).call(this, new r.SpriteMaterial({
                map: new r.Texture
            })));
            return i._text = t, i._textHeight = e, i._color = n, i._fontFace = "Arial", i._fontSize = 90, i._canvas = document.createElement("canvas"), i._texture = i.material.map, i._texture.minFilter = r.LinearFilter, i._genCanvas(), i
        }
        return function(t, e) {
            
            if ("function" != typeof e && null !== e) throw new TypeError("Super expression must either be null or a function, not " + typeof e);
            t.prototype = Object.create(e && e.prototype, {
                constructor: {
                    value: t,
                    enumerable: !1,
                    writable: !0,
                    configurable: !0
                }
            }), e && (Object.setPrototypeOf ? Object.setPrototypeOf(t, e) : t.__proto__ = e)
        }(o, r.Sprite), e(o, [{
            key: "_genCanvas",
            value: function() {
                var t = this._canvas,
                    e = t.getContext("2d"),
                    n = "normal " + this.fontSize + "px " + this.fontFace;
                e.font = n;
                var i = e.measureText(this.text).width;
                var col = bgColor
                if(this.fontSize === 49){
                    col = bgColor2
                }
                if(this.fontSize === 48 || this.fontSize === 49  ){
                    t.width = i, t.height = this.fontSize,e.fillStyle = col, e.fillRect(0,0,t.width, t.height), e.font = n, e.fillStyle = this.color, e.textBaseline = "bottom", e.fillText(this.text, 0, t.height), this._texture.image = t, this._texture.needsUpdate = !0, this.scale.set(this.textHeight * t.width / t.height, this.textHeight);
            }else{
t.width = i, t.height = this.fontSize,e.font = n, e.fillStyle = this.color, e.textBaseline = "bottom", e.fillText(this.text, 0, t.height), this._texture.image = t, this._texture.needsUpdate = !0, this.scale.set(this.textHeight * t.width / t.height, this.textHeight);
                }
            }
        }, {
            key: "clone",
            value: function() {
                return new this.constructor(this.text, this.textHeight, this.color).copy(this)
            }
        }, {
            key: "copy",
            value: function(t) {
                return r.Sprite.prototype.copy.call(this, t), this.color = t.color, this.fontFace = t.fontFace, this.fontSize = t.fontSize, this
            }
        }, {
            key: "text",
            get: function() {
                return this._text
            },
            set: function(t) {
                this._text = t, this._genCanvas()
            }
        }, {
            key: "textHeight",
            get: function() {
                return this._textHeight
            },
            set: function(t) {
                this._textHeight = t, this._genCanvas()
            }
        }, {
            key: "color",
            get: function() {
                return this._color
            },
            set: function(t) {
                this._color = t, this._genCanvas()
            }
        }, {
            key: "fontFace",
            get: function() {
                return this._fontFace
            },
            set: function(t) {
                this._fontFace = t, this._genCanvas()
            }
        }, {
            key: "fontSize",
            get: function() {
                return this._fontSize
            },
            set: function(t) {
                this._fontSize = t, this._genCanvas()
            }
        }]), o
    }()
});