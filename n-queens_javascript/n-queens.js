function Board(n) {
    this.bsize = n;       // board size = n x n
    this.found = 0;
    this.wait = 100;
    const MODE_PLAY=0, MODE_PAUSED=1, MODE_STEP=2, MODE_STOP = 3;
    this.mode = MODE_PLAY;
    this.saved_state = undefined;

    // return an n*n array
    this.place = function(queens) {
        return Array.from({length: this.bsize}, (_,row) => {
            return Array.from({length: this.bsize}, (_,col) => {
                if (queens[row] === col) {
                    return 'Q';       // a queen here
                }
                else if (queens.length > row) {
                    return '+';      // a queen on this row
                }
                else if (queens.some((c, r) => (
                    (c === col) ||  // a queen on this column
                    r - row == c - col || r - row == col - c)))   // diagonal
                    return '+';
                else
                    return ' ';      // available
            });
        });
    };

    this.putQueens = function() {
	this.tryNewRow([]);
    };

    this.stop = function() { this.mode = MODE_STOP; };

    this.play = function(w) {
        let oldmode = this.mode;
        this.wait = w;
        this.mode = MODE_PLAY;

        if (oldmode === MODE_PAUSED && this.saved_state) {
            this.cont();
        }
    };

    this.step = function() {
        let oldmode = this.mode;
        this.mode = MODE_STEP;

        if (oldmode === MODE_PAUSED && this.saved_state)
            this.cont();
    };
            
    this.pause = function() { this.mode = MODE_PAUSED; };


    this.cont = function() {
        if (this.mode !== MODE_STOP) {
            let queens = this.saved_state[0],
                col = this.saved_state[1];

            this.saved_state = undefined;
            this.tryCol(queens, col);
        }
    };

    this.tryNewRow = function(queens) {
        this.afterRefresh(queens, this.tryCol, queens, 0);
    };

    this.tryCol = function(queens, col) {
        if (this.mode === MODE_PAUSED) {
            this.saved_state = [queens, col];
            return;
        }
        else if (this.mode === MODE_STEP)
            this.mode = MODE_PAUSED;


        let marks = this.place(queens),
            row = queens.length;

        while (col < this.bsize && marks[row][col] !== ' ')
            ++col;

        if (col >= this.bsize) {
            // no more posible columns in this row
            if (queens.length === 0) {
                // done for all rows
            }
            else {
                let newcol = queens.pop();
                this.afterRefresh(queens, this.tryCol, queens, newcol+1);
            }
        }
        else if (queens.length == this.bsize -1) {
            queens.push(col);
            this.found++;
            record_result(this, queens);
            this.afterRefresh(queens, (q, c) =>{
                q.pop();
                this.afterRefresh(q, this.tryCol, q, c+1);
            }, queens, col);
        }
        else {
            queens.push(col);
            this.tryNewRow(queens);
        }
    };

    this.afterDelay = function(wait, fun, args) {
        let p1 = new Promise(function (resolve) {
            setTimeout((a) => resolve(a), wait, args);});
        p1.then((_a) => {
            if (this.mode !== MODE_STOP)
                fun.apply(this, _a);
        });
    };

    this.afterRefresh = function() {
        let args = Array.prototype.slice.call(arguments),
            queens = args.shift(),
            fun = args.shift(),
            wait = this.wait;
        
        refresh_display(this, queens);
        this.afterDelay(wait, fun, args);
    };


    this.toShow = function(positions, flag) {
        let seq = Array.from({length: n}, (v,k) => k),
            marks = undefined,
	    style = flag ? '' : 'style="float: left;"';

        if (flag)
            marks = this.place(positions);

        return "<table border " + style + ">" +
            seq.map( row => "<tr>" +
		     seq.map( col => {
		         var text = positions[row] === col ? "\u2655" : "",
			     cls = 'tdvalid';
		         if (marks) {
                             switch (marks[row][col]) {
                             case '+' : cls = 'tdinval'; break;
                             case 'Q' : cls = 'tdqueen'; break;
                             case ' ' : cls = 'tdvalid'; break;
                             }
                         }
		         else
			     cls = (row + col) % 2 === 0 ? "tdeven" : "tdodd";
		         return '<td class="'  + cls + '">' + text;
		     }).join('') +
		     "</tr>").join("\n") +
	    "</table>";
    };

}


////////////////////////////////////////////////////////////////
//


function record_result(board, positions) {
    console.log("found! " + positions);
    document.getElementsByName("results")[0].insertAdjacentHTML("afterbegin",
                                                                board.toShow(positions, false));
    document.getElementsByName("result_count")[0].innerHTML =
        "found %d patterns".replace("%d", board.found);
}

function refresh_display(board, positions) {
    document.getElementsByName("main")[0].innerHTML = board.toShow(positions, true);
}

function clear_display() {
    document.getElementsByName("result_count")[0].innerHTML = "found 0 patterns";
    document.getElementsByName("results")[0].innerHTML = "";

}
