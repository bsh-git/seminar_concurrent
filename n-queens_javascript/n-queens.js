function Board(n) {
    this.bsize = n;       // board size = n x n
    this.found = 0;

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

    this.tryNewRow = function(queens) {
        this.afterRefresh(queens, this.tryCol, queens, 0);
    };

    this.tryCol = function(queens, col) {
        let marks = this.place(queens),
            row = queens.length;
        console.log(row + "," + col + "," + queens);

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
            queens.pop();
            this.afterRefresh(queens, this.tryCol, queens, col+1);
        }
        else {
            queens.push(col);
            this.tryNewRow(queens);
        }
    };

    this.afterRefresh = function() {
        let args = Array.prototype.slice.call(arguments),
            queens = args.shift(),
            fun = args.shift();
        
        refresh_display(this, queens);
        let p1 = new Promise(function (resolve) {
            setTimeout((a) => resolve(a), 1, args);});
        p1.then((args) => fun.apply(this, args));

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
